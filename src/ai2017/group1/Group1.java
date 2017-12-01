package ai2017.group1;

import ai2017.group1.boa.acceptance.Acceptance;
import ai2017.group1.boa.bidding.Offering;
import ai2017.group1.boa.opponent.Bidding;
import ai2017.group1.boa.opponent.FrequencyModel;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Group 1 2017 negotiation agent implementing the BOA framework.
 */
public class Group1 extends AbstractNegotiationParty {

	protected Acceptance acceptConditions;
	protected Offering offeringStrategy;
	protected FrequencyModel opponentModel;
	protected Bidding omStrategy;
	protected NegotiationSession negotiationSession;
	private Bid oppBid;
	private List<Tuple<Double, Double>> myBids = new ArrayList<>();

	private boolean noOfOpponentsPassed = false;

	/**
	 * Initialize with negotiation session and strategies.
	 *
	 * @param session retrieved from GENIUS.
	 */
	@Override
	public void init(NegotiationInfo session) {
		super.init(session);
		this.negotiationSession = new NegotiationSession(new SessionData(), this.utilitySpace, this.timeline);
		this.initStrategies();
	}

	/**
	 * Initialize all BOA strategies with given parameters.
	 * Current parameters values have shown to perform best from our analysis.
	 */
	private void initStrategies() {
		try {
			opponentModel = new FrequencyModel();
			omStrategy = new Bidding();
			offeringStrategy = new Offering();
			acceptConditions = new Acceptance();
			Map<String, Double> parameters = new HashMap<String, Double>() {{
				put("l", 0.1);
				put("t", 1.1);
				put("e", 0.5);
				put("k", 0.0);
				put("a", 1.0);
				put("b", 0.0);
				put("min", 0.9);
			}};
			this.opponentModel.init(this.negotiationSession, parameters);
			this.omStrategy.init(this.negotiationSession, this.opponentModel, parameters);
			this.offeringStrategy.init(this.negotiationSession, this.opponentModel, this.omStrategy, parameters);
			this.acceptConditions.init(this.negotiationSession, this.offeringStrategy, this.opponentModel, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * When the agent receives a message, strategies define what to do with this message.
	 *
	 * @param sender         agent that sent the opponent action
	 * @param opponentAction type of move made by the opponent
	 */
	public void receiveMessage(AgentID sender, Action opponentAction) {
		super.receiveMessage(sender, opponentAction);
		if (getNumberOfParties() != -1 && noOfOpponentsPassed == false) {
			offeringStrategy.setNoOfOpponents(getNumberOfParties() - 1);
			opponentModel.setNoOfOpponents(getNumberOfParties() - 1);
			noOfOpponentsPassed = true;
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
			} catch (Exception e) {
				e.printStackTrace();
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

	/**
	 * Choose an action based on the offering strategy.
	 *
	 * @param possibleActions type of actions possible
	 * @return actoin to take.
	 */
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

	/**
	 * Write out log file for analysis.
	 *
	 * @param acceptedBid the accepting bid ends the negotiation.
	 * @return null
	 */
	public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
		try {
			String logFolder = "log";
			Properties props = System.getProperties();
			if (props.containsKey("logfolder")) {
				logFolder = props.getProperty("logfolder");
			}
			PrintWriter writer = new PrintWriter("logfolder.txt", "UTF-8");
			writer.println(logFolder);
			writer.close();
			DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
			Date date = new Date();
			PrintWriter pw = new PrintWriter(new File(logFolder + "/our_" + dateFormat.format(date) + ".csv"));
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
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDescription() {
		return "Agent Group1 2017";
	}

}
