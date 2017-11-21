package ai2017.group1;

import ai2017.group1.boa.acceptance.AC_Next;
import ai2017.group1.boa.acceptance.Group1_Accept;
import ai2017.group1.boa.bidding.TimeDependent_Offering;
import ai2017.group1.boa.opponent.BestBid;
import ai2017.group1.boa.opponent.HardHeadedFrequencyModel;
import list.Tuple;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.Actions;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.SessionData;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import misc.Pair;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * This is your negotiation party.
 */
public class Group1 extends AbstractNegotiationParty {

	protected Group1_Accept acceptConditions;
	protected TimeDependent_Offering offeringStrategy;
	protected HardHeadedFrequencyModel opponentModel;
	protected BestBid omStrategy;
	protected NegotiationSession negotiationSession;
	private Bid oppBid;
	private List<Tuple<Double, Double>> myBids = new ArrayList<>();

	@Override
	public void init(NegotiationInfo info) {

		super.init(info);

		this.negotiationSession = new NegotiationSession(new SessionData(), this.utilitySpace, this.timeline);
		this.initStrategies();
	}

	private void initStrategies() {
		try {
			opponentModel = new HardHeadedFrequencyModel();
			omStrategy = new BestBid();
			offeringStrategy = new TimeDependent_Offering();
			acceptConditions = new Group1_Accept();
			Map<String, Double> parameters = new HashMap<String, Double>() {{
				put("l", 0.1);
				put("t", 1.1);
				put("e", 10.0);
				put("k", 0.0);
				put("a", 1.0);
				put("b", 0.0);
			}};
			this.opponentModel.init(this.negotiationSession, parameters);
			this.omStrategy.init(this.negotiationSession, this.opponentModel, parameters);
			this.offeringStrategy.init(this.negotiationSession, this.opponentModel, this.omStrategy, parameters);
			this.acceptConditions.init(this.negotiationSession, this.offeringStrategy, this.opponentModel, parameters);
		} catch (Exception var2) {
			var2.printStackTrace();
		}
	}

    public void receiveMessage(AgentID sender, Action opponentAction) {
	    super.receiveMessage(sender, opponentAction);
	    if (getNumberOfParties() != -1) {
	        offeringStrategy.setNoOfOpponents(getNumberOfParties() - 1);
            opponentModel.setNoOfOpponents(getNumberOfParties() - 1);
        }
        if (opponentAction instanceof Offer || opponentAction instanceof Accept) {
            if (opponentAction instanceof Offer) {
                this.oppBid = ((Offer) opponentAction).getBid();
            } else {
                this.oppBid = ((Accept) opponentAction).getBid();
            }

            try {
                BidDetails opponentBid = new BidDetails(this.oppBid, this.negotiationSession.getUtilitySpace().getUtility(this.oppBid), this.negotiationSession.getTime());
                this.negotiationSession.getOpponentBidHistory().add(opponentBid);
            } catch (Exception var4) {
                var4.printStackTrace();
            }

            if (this.opponentModel != null) {
                if (this.omStrategy.canUpdateOM()) {
                    this.opponentModel.updateModel(this.oppBid);
                } else if (!this.opponentModel.isCleared()) {
                    this.opponentModel.cleanUp();
                }
            }
        }

    }

    public Action chooseAction(List<Class<? extends Action>> possibleActions) {
        BidDetails bid;
        if (this.negotiationSession.getOwnBidHistory().getHistory().isEmpty()) {
            bid = this.offeringStrategy.determineOpeningBid();
        } else {
            bid = this.offeringStrategy.determineNextBid();
            if (this.offeringStrategy.isEndNegotiation()) {
                return new EndNegotiation(this.getPartyId());
            }
        }
//        myBids.put(new Double(this.timeline.getTime()).toString(), new Double(bid.getMyUndiscountedUtil()).toString());
        myBids.add(new Tuple<>(timeline.getTime(), bid.getMyUndiscountedUtil()));

        if (bid == null) {
            System.out.println("Error in code, null bid was given");
            return new Accept(this.getPartyId(), this.oppBid);
        } else {
            this.offeringStrategy.setNextBid(bid);
            Actions decision = Actions.Reject;
            if (!this.negotiationSession.getOpponentBidHistory().getHistory().isEmpty()) {
                decision = this.acceptConditions.determineAcceptability();
            }

            if (decision.equals(Actions.Break)) {
                System.out.println("send EndNegotiation");
                return new EndNegotiation(this.getPartyId());
            } else if (decision.equals(Actions.Reject)) {
                this.negotiationSession.getOwnBidHistory().add(bid);
                return new Offer(this.getPartyId(), bid.getBid());
            } else {
                return new Accept(this.getPartyId(), this.oppBid);
            }
        }
    }
//
//    public void endSession(NegotiationResult result) {
//        this.offeringStrategy.endSession(result);
//        this.acceptConditions.endSession(result);
//        this.opponentModel.endSession(result);
//        SessionData savedData = this.negotiationSession.getSessionData();
//        if (!savedData.isEmpty() && savedData.isChanged()) {
//            savedData.changesCommitted();
//            this.getData().put(savedData);
//        }
//
//    }
    public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
            Date date = new Date();
            PrintWriter pw = new PrintWriter(new File("log/our_" + dateFormat.format(date) + ".csv"));
            StringBuilder sb = new StringBuilder();
            for (Tuple<Double, Double> myBid : myBids) {
                sb.append(myBid.get1());
                sb.append(";");
                sb.append(myBid.get2());
                sb.append("\n");
            }
            pw.write(sb.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

	@Override
	public String getDescription() {
		return "example party group 1";
	}

}
