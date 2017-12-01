package ai2017.group1.boa.bidding;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.BOAparameter;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.NoModel;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;


/**
 * Bidding strategy Group 1 2017.
 * This class was partially based on the Timedependent offering class offered as an example in the BOA framework.
 */
public class Offering extends OfferingStrategy {

	private double k;
	private double Pmax;
	private double Pmin;
	private double e;
	private double delta = 0;
	private int noOfOpponents;
	private SortedOutcomeSpace outcomespace;

	/**
	 * Initialize strategy.
	 *
	 * @param session    negotiation sessions set by GENIUS.
	 * @param model      opponent model used.
	 * @param oms        opponent model strategy used.
	 * @param parameters parameters including value for 'e'.
	 * @throws Exception if parameter 'e' was not set.
	 */
	@Override
	public void init(NegotiationSession session, OpponentModel model, OMStrategy oms,
					 Map<String, Double> parameters) throws Exception {
		super.init(session, parameters);
		if (parameters.get("e") != null) {
			this.negotiationSession = session;

			outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
			negotiationSession.setOutcomeSpace(outcomespace);

			this.e = parameters.get("e");
			Properties props = System.getProperties();
			if (props.containsKey("parame")) {
				this.e = Double.parseDouble(props.getProperty("parame"));
			}
			PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
			writer.println("Param e:" + this.e);

			if (parameters.get("k") != null)
				this.k = parameters.get("k");
			else
				this.k = 0;

			if (parameters.get("min") != null)
				this.Pmin = parameters.get("min");
			else
				this.Pmin = session.getMinBidinDomain().getMyUndiscountedUtil();
			if (props.containsKey("paramf")) {
				this.Pmin = Double.parseDouble(props.getProperty("paramf"));
			}
			writer.println("Param f:" + this.Pmin);
			writer.close();

			if (parameters.get("max") != null) {
				Pmax = parameters.get("max");
			} else {
				BidDetails maxBid = session.getMaxBidinDomain();
				Pmax = maxBid.getMyUndiscountedUtil();
			}

			this.opponentModel = model;
			this.omStrategy = oms;
		} else {
			throw new Exception("Constant \"e\" for the concession speed was not set.");
		}
	}

	/**
	 * When we can make the opening bid, determine this bid.
	 *
	 * @return determined bid
	 */
	@Override
	public BidDetails determineOpeningBid() {
		return determineNextBid();
	}

	/**
	 * Determine the next bid based on opponent model and utility goal.
	 *
	 * @return next bid.
	 */
	@Override
	public BidDetails determineNextBid() {
		double time = negotiationSession.getTime();
		double utilityGoal;
		if (time > 0.95) {
			delta = 0;
		} else if (noOfOpponents > -1) {
			delta = determineDelta(time);
		}
		utilityGoal = p(time);

		// Check if an opponent model was set.
		if (opponentModel instanceof NoModel) {
			nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(utilityGoal);
		} else {
			nextBid = omStrategy.getBid(outcomespace, utilityGoal);
		}
		return nextBid;
	}

	/**
	 * Determine the value for delta based on time.
	 *
	 * @param t time
	 * @return delta
	 */
	private double determineDelta(double t) {
		double d = delta;

		double currentUtil = getAvgUtil(0);
		double prevUtil = getAvgUtil(1);
		if (currentUtil != -1 && prevUtil != -1) {
			if (currentUtil < prevUtil) {
				d += 0.01;
				if (d > Pmax - p(t)) {
					d = Pmax - p(t);
				}
			} else {
				d -= 0.01;
				if (d < 0) {
					d = 0;
				}
			}
		}
		return d;
	}

	/**
	 * Get the average utility for a certain round.
	 *
	 * @param round given round in negotiation
	 * @return average utility
	 */
	private double getAvgUtil(int round) {
		double avgUtil = -1;
		if (negotiationSession.getOpponentBidHistory().size() >= noOfOpponents + 1) {
			BidDetails oppBids[] = new BidDetails[noOfOpponents];
			for (int i = 0; i < noOfOpponents; i++) {
				oppBids[i] = negotiationSession.getOpponentBidHistory().getHistory()
						.get(negotiationSession.getOpponentBidHistory().size() - (i + 1 + round));
			}

			double ownUtil = 0;
			for (BidDetails oppBid : oppBids) {
				ownUtil += oppBid.getMyUndiscountedUtil();
			}
			avgUtil = ownUtil / noOfOpponents;
		}
		return avgUtil;
	}

	/**
	 * Compute f(t).
	 *
	 * @param t time
	 * @return ft
	 */
	public double f(double t) {
		if (e == 0)
			return k;
		double ft = k + (1 - k) * Math.pow(t, e);
		return ft;
	}

	/**
	 * Compute p(t)
	 *
	 * @param t time
	 * @return p
	 */
	public double p(double t) {
		return Pmin + (Pmax - Pmin) * (1 - f(t)) + delta;
	}

	/**
	 * Get the negotiation session
	 *
	 * @return negotiation session
	 */
	public NegotiationSession getNegotiationSession() {
		return negotiationSession;
	}

	/**
	 * Get the parameters set for BOA framework.
	 *
	 * @return parameters
	 */
	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("e", 1.0, "Concession rate"));
		set.add(new BOAparameter("k", 0.0, "Offset"));
		set.add(new BOAparameter("min", 0.0, "Minimum utility"));
		set.add(new BOAparameter("max", 0.99, "Maximum utility"));

		return set;
	}

	@Override
	public String getName() {
		return "Group 1 2017 offering strategy";
	}

	public void setNoOfOpponents(int noOfOpponents) {
		this.noOfOpponents = noOfOpponents;
	}
}