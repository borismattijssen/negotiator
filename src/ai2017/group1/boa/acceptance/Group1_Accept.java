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

public class Group1_Accept extends AcceptanceStrategy{

    private double a;
    private double b;

    /**
     * Empty constructor for the BOA framework.
     */
    public Group1_Accept() {
    }

    public Group1_Accept(NegotiationSession negoSession, OfferingStrategy strat, double alpha, double beta) {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;
        this.a = alpha;
        this.b = beta;
    }

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

    @Override
    public Actions determineAcceptability() {
        double nextMyBidUtil = offeringStrategy.getNextBid().getMyUndiscountedUtil();
        double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails()
                .getMyUndiscountedUtil();
        double minimumUtility = 0.95;

        /* Set a and b based on time lef */
        if () {

        }

        /* Set minimum utility based on time left */
        if () {

        }
        double timePassed = negotiationSession.getTime();

        /* Accept opponent bid if the utility is higher than the utility of own previous bid */
        if (!negotiationSession.getOwnBidHistory().getHistory().isEmpty()) {
            double prevMyBidUtil = negotiationSession.getOwnBidHistory().getLastBidDetails()
                    .getMyUndiscountedUtil();
            if (lastOpponentBidUtil >= prevMyBidUtil) {
                return Actions.Accept;
            }
        }
        /* Accept opponent bid if the utility is higher than the utility of own next bid */
        else if (((a * lastOpponentBidUtil) + b) >= nextMyBidUtil) {
            /* insert changes to a and b based on time left and concessions level, for next deadline */
            return Actions.Accept;
        }
        /* Accept opponent bid if the utility is above a set value */
        else if(lastOpponentBidUtil >= minimumUtility){
            /* insert changes to minimum utility acceptability based on time left and concessions level, for next deadline */
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
        return "Group1_Accept example";
    }

}
