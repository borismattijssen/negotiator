package ai2017.group1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai2017.group1.boa.acceptance.AC_Next;
import ai2017.group1.boa.bidding.TimeDependent_Offering;
import ai2017.group1.boa.opponent.BestBid;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.Actions;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.SessionData;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

import ai2017.group1.boa.opponent.HardHeadedFrequencyModel;


/**
 * This is your negotiation party.
 */
public class Group1_old extends AbstractNegotiationParty {

	private Bid optimal = null;
	private boolean doAccept = false;

	private HardHeadedFrequencyModel opponentModel;
	private BestBid opponentModelStrategy;
	private TimeDependent_Offering offeringStrategy;
	private AC_Next acceptanceStrategy;
	private NegotiationSession negotiationSession;

	@Override
	public void init(NegotiationInfo info) {

		super.init(info);

		System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
		System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());

		try {
			optimal = info.getUtilitySpace().getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}

		TimeLineInfo timeline = this.getTimeLine();
		AbstractUtilitySpace utilitySpace = this.getUtilitySpace();
		SessionData sessionData = new SessionData();
		negotiationSession = new NegotiationSession(sessionData, utilitySpace, timeline);

		Map<String, Double> parameters = new HashMap<String, Double>() {{
			put("l", 0.1);
			put("t", 1.1);
			put("e", 1.0);
			put("k", 0.0);
			put("a", 1.0);
			put("b", 0.0);
		}};

		opponentModel = new HardHeadedFrequencyModel();
		opponentModelStrategy = new BestBid();
		offeringStrategy = new TimeDependent_Offering();
		acceptanceStrategy = new AC_Next();

		opponentModel.init(negotiationSession, parameters);
		opponentModelStrategy.init(negotiationSession, opponentModel, parameters);

		try {
			offeringStrategy.init(negotiationSession, opponentModel, opponentModelStrategy, parameters);
			acceptanceStrategy.init(negotiationSession, offeringStrategy, opponentModel, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
	    offeringStrategy.determineNextBid();
		if (validActions.contains(Accept.class)) {
		    if (acceptanceStrategy.determineAcceptability() == Actions.Accept) {
                return new Accept(this.getPartyId(), negotiationSession.getOpponentBidHistory().getLastBid());
            }
		}
		return new Offer(this.getPartyId(), offeringStrategy.getNextBid().getBid());
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);

		if (action instanceof Accept) {
			Accept accept = (Accept) action;
		}
		else if (action instanceof Bid) {
			Bid bid = (Bid) action;

            negotiationSession.getOpponentBidHistory().add(new BidDetails(bid, 1));
			opponentModel.updateModel(bid, this.timeline.getTime());
		}
		else {
			System.out.println(action);
		}

//		if (action instanceof Accept) {
//			Accept acc = (Accept) action;
//			if (acc.getBid().equals(optimal)) {
//				doAccept = true;
//			}
//		}
	}

	@Override
	public String getDescription() {
		return "example party group 1";
	}

}
