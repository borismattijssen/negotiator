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
 */
public class Offering extends OfferingStrategy {


	private double k;
	private double Pmax;
	private double Pmin;
	private double e;

	private double delta=0;
	private int noOfOpponents;
	/** Outcome space */
	private SortedOutcomeSpace outcomespace;

	/**
	 * Method which initializes the agent by setting all parameters. The
	 * parameter "e" is the only parameter which is required.
	 */
	@Override
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms,
			Map<String, Double> parameters) throws Exception {
		super.init(negoSession, parameters);
		if (parameters.get("e") != null) {
			this.negotiationSession = negoSession;

			outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
			negotiationSession.setOutcomeSpace(outcomespace);

			this.e = parameters.get("e");
			Properties props = System.getProperties();
			if(props.containsKey("parame")) {
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
				this.Pmin = negoSession.getMinBidinDomain().getMyUndiscountedUtil();
			if(props.containsKey("paramf")) {
				this.Pmin = Double.parseDouble(props.getProperty("paramf"));
			}
			writer.println("Param f:" + this.Pmin);
			writer.close();

			if (parameters.get("max") != null) {
				Pmax = parameters.get("max");
			} else {
				BidDetails maxBid = negoSession.getMaxBidinDomain();
				Pmax = maxBid.getMyUndiscountedUtil();
			}

			this.opponentModel = model;
			this.omStrategy = oms;
		} else {
			throw new Exception("Constant \"e\" for the concession speed was not set.");
		}
	}

	@Override
	public BidDetails determineOpeningBid() {
		return determineNextBid();
	}

	/**
	 * Simple offering strategy which retrieves the target utility and looks for
	 * the nearest bid if no opponent model is specified. If an opponent model
	 * is specified, then the agent return a bid according to the opponent model
	 * strategy.
	 */
	@Override
	public BidDetails determineNextBid() {
		double time = negotiationSession.getTime();
		double utilityGoal;
		if (time > 0.95) {
			delta = 0;
		} else if(noOfOpponents > -1) {
			delta = determineDelta(time);
		}
		utilityGoal = p(time);

		// if there is no opponent model available
		if (opponentModel instanceof NoModel) {
			nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(utilityGoal);
		} else {
			nextBid = omStrategy.getBid(outcomespace, utilityGoal);
		}
		return nextBid;
	}

	private double determineDelta(double t) {
		double d = delta;

		double currentUtil = getAvgUtil(0);
		double prevUtil = getAvgUtil(1);
		if (currentUtil != -1 && prevUtil != -1) {
			if (currentUtil < prevUtil) {
				d += 0.01;
				if (d > Pmax - p(t)){
					d = Pmax - p(t);
				}
			}
			else {
				d -= 0.01;
				if (d < 0) {
					d = 0;
				}
			}
		}
		return d;
	}

	private double getAvgUtil(int round) {
		double avgUtil = -1;
		if (negotiationSession.getOpponentBidHistory().size() >= noOfOpponents + 1) {
			BidDetails oppBids[] = new BidDetails[noOfOpponents];
			for (int i=0; i<noOfOpponents; i++) {
				oppBids[i] = negotiationSession.getOpponentBidHistory().getHistory()
						.get(negotiationSession.getOpponentBidHistory().size() - (i+1+round));
			}

			double ownUtil = 0;
			for (BidDetails oppBid : oppBids) {
				ownUtil += oppBid.getMyUndiscountedUtil();
			}
			avgUtil = ownUtil/noOfOpponents;
		}

		return avgUtil;
	}

	/**
	 * From [1]:
	 *
	 * A wide range of time dependent functions can be defined by varying the
	 * way in which f(t) is computed. However, functions must ensure that 0 <=
	 * f(t) <= 1, f(0) = k, and f(1) = 1.
	 *
	 * That is, the offer will always be between the value range, at the
	 * beginning it will give the initial constant and when the deadline is
	 * reached, it will offer the reservation value.
	 *
	 * For e = 0 (special case), it will behave as a Hardliner.
	 */
	public double f(double t) {
		if (e == 0)
			return k;
		double ft = k + (1 - k) * Math.pow(t, e);
		return ft;
	}

	/**
	 * Makes sure the target utility with in the acceptable range according to
	 * the domain Goes from Pmax to Pmin!
	 * 
	 * @param t
	 * @return double
	 */
	public double p(double t) {
		return Pmin + (Pmax - Pmin) * (1 - f(t)) + delta;
	}

	public NegotiationSession getNegotiationSession() {
		return negotiationSession;
	}

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
		return "TimeDependent Offering example";
	}

	public void setNoOfOpponents(int noOfOpponents) {
		this.noOfOpponents = noOfOpponents;
	}
}