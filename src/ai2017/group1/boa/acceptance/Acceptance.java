package ai2017.group1.boa.acceptance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.Actions;
import negotiator.boaframework.BOAparameter;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;

/**
 * Acceptance strategy Group 1 2017.
 */
public class Acceptance extends AcceptanceStrategy{

    private double a;
    private double b;

    /**
     * Empty constructor for the BOA framework.
     */
    public Acceptance() {
    }

    /**
     * Constructor with parameters
     *
     * @param negoSession negotiation session as set by GENIUS.
     * @param strat BOA offering strategy
     * @param alpha alpha value
     * @param beta beta value
     */
    public Acceptance(NegotiationSession negoSession, OfferingStrategy strat, double alpha, double beta) {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;
        this.a = alpha;
        this.b = beta;
    }

    /**
     * Initialize acceptance strategy
     *
     * @param negoSession negotiation session as set by GENIUS.
     * @param strat BOA offering strategy
     * @param opponentModel BOA opponent model
     * @param parameters value parameters
     * @throws Exception
     */
    @Override
    public void init(NegotiationSession negoSession, OfferingStrategy strat, OpponentModel opponentModel,
                     Map<String, Double> parameters) throws Exception {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;

        if (parameters.get("a") != null || parameters.get("b") != null) {
            a = parameters.get("a");
            b = parameters.get("b");
        } else {
            a = 1;
            b = 0;
        }
    }

    @Override
    public String printParameters() {
        return "[a: " + a + " b: " + b + "]";
    }

    /**
     * Actual strategy implemented.
     *
     * @return accept or reject action.
     */
    @Override
    public Actions determineAcceptability() {
        double nextMyBidUtil = offeringStrategy.getNextBid().getMyUndiscountedUtil();
        double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails()
                .getMyUndiscountedUtil();
        double minimumUtility = 0.95;
        double timePassed = negotiationSession.getTime();

        // Set minimum utility based on time left
        if (timePassed >= 0.98) {
            minimumUtility = 0.7;
        }
        else if(timePassed >= 0.95){
            minimumUtility = 0.9;
        }
        // Accept opponent bid if the utility is higher than the utility of own previous bid
        if (!negotiationSession.getOwnBidHistory().getHistory().isEmpty()) {
            double prevMyBidUtil = negotiationSession.getOwnBidHistory().getLastBidDetails()
                    .getMyUndiscountedUtil();
            if (lastOpponentBidUtil >= prevMyBidUtil) {
                return Actions.Accept;
            }
        }
        // Accept opponent bid if the utility is higher than the utility of own next bid
        else if (((a * lastOpponentBidUtil) + b) >= nextMyBidUtil) {
            return Actions.Accept;
        }
        // Accept opponent bid if the utility is above a set value the minimumUtility is set to change over time
        else if(lastOpponentBidUtil >= minimumUtility){
            return Actions.Accept;
        }
        return Actions.Reject;

    }

    @Override
    public Set<BOAparameter> getParameterSpec() {
        Set<BOAparameter> set = new HashSet<BOAparameter>();
        set.add(new BOAparameter("a", 1.0,
                "Accept when the opponent's utility * a + b is greater than the utility of our current bid"));
        set.add(new BOAparameter("b", 0.0,
                "Accept when the opponent's utility * a + b is greater than the utility of our current bid"));
        return set;
    }

    @Override
    public String getName() {
        return "Acceptance strategy";
    }

}
