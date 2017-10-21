package ai2017.group1;

import ai2017.group1.boa.acceptance.AC_Next;
import ai2017.group1.boa.bidding.TimeDependent_Offering;
import ai2017.group1.boa.opponent.BayesianModel;
import ai2017.group1.boa.opponent.BestBid;
import ai2017.group1.boa.opponent.HardHeadedFrequencyModel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is your negotiation party.
 */
public class Group1 extends AbstractNegotiationParty {

	protected AC_Next acceptConditions;
	protected TimeDependent_Offering offeringStrategy;
	protected HardHeadedFrequencyModel omFrequency;
	protected BayesianModel omBayesian;
	protected BestBid omStrategy;
	protected NegotiationSession negotiationSession;
	private Bid oppBid;

	@Override
	public void init(NegotiationInfo info) {

		super.init(info);

		this.negotiationSession = new NegotiationSession(new SessionData(), this.utilitySpace, this.timeline);
		this.initStrategies();
	}

	private void initStrategies() {
		try {
			omFrequency = new HardHeadedFrequencyModel();
			omBayesian = new BayesianModel();
			omStrategy = new BestBid();

			offeringStrategy = new TimeDependent_Offering();
			acceptConditions = new AC_Next();

			Map<String, Double> parameters = new HashMap<String, Double>() {{
				put("l", 0.5);
				put("t", 1.1);
				put("e", 3.0);
				put("k", 0.0);
				put("a", 1.0);
				put("b", 0.0);
			}};
			this.omFrequency.init(this.negotiationSession, parameters);
			this.omBayesian.init(this.negotiationSession, parameters);
			this.omStrategy.init(this.negotiationSession, this.omFrequency, parameters);

			this.offeringStrategy.init(this.negotiationSession, this.omFrequency, this.omStrategy, parameters);
			this.acceptConditions.init(this.negotiationSession, this.offeringStrategy, this.omFrequency, parameters);
		} catch (Exception var2) {
			var2.printStackTrace();
		}
	}

    public void receiveMessage(AgentID sender, Action opponentAction) {
	    super.receiveMessage(sender, opponentAction);
	    if (getNumberOfParties() != -1) {
            omFrequency.setNoOfOpponents(getNumberOfParties() - 1);
			omBayesian.setNoOfOpponents(getNumberOfParties() - 1);
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

			// Update frequency opponent model
            if (this.omFrequency != null) {
                if (this.omStrategy.canUpdateOM()) {
                    this.omFrequency.updateModel(this.oppBid);
                } else if (!this.omFrequency.isCleared()) {
                    this.omFrequency.cleanUp();
                }
            }

			// Update bayesian opponent model
            if (this.omBayesian != null) {
            	if (this.omStrategy.canUpdateOM()) {
            		this.omBayesian.updateModel(this.oppBid, this.negotiationSession.getTime());
				} else if (!this.omBayesian.isCleared()) {
            		this.omBayesian.cleanUp();
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
				this.negotiationSession.getOwnBidHistory().add(bid);
				return new Offer(this.getPartyId(), bid.getBid());
            }
        }
    }
//
//    public void endSession(NegotiationResult result) {
//        this.offeringStrategy.endSession(result);
//        this.acceptConditions.endSession(result);
//        this.omFrequency.endSession(result);
//        SessionData savedData = this.negotiationSession.getSessionData();
//        if (!savedData.isEmpty() && savedData.isChanged()) {
//            savedData.changesCommitted();
//            this.getData().put(savedData);
//        }
//
//    }

	@Override
	public String getDescription() {
		return "example party group 1";
	}

}
