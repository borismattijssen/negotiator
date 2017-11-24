package ai2017.group1.boa.opponent;

import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.BOAparameter;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.issue.Issue;
import negotiator.issue.Value;

import java.util.*;

/**
 * Frequency model implemented as learning in class.
 */
public class FrequencyModel extends OpponentModel {

	private int noOfOpponents = -1;
	private double learnCoef = 0.1;

	private HashMap<Integer, HashMap<Issue, Double>> opponentWeights;
	private HashMap<Integer, HashMap<Issue, HashMap<Value, Double>>> opponentFrequencies;
	private HashMap<Integer, HashMap<Issue, HashMap<Value, Double>>> opponentUtilities;

	/**
	 * Initialize the frequency opponent model
	 *
	 * @param negotiationSession contains all information on negotiation
	 * @param parameters set for the frequency model. Includes the learning coefficient.
	 */
	@Override
	public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
		// Initialize opponentmodel and set negotiation session
		super.init(negotiationSession, parameters);
		this.negotiationSession = negotiationSession;

		// Set learning coefficient from parameters. Default is 0.1
		if (parameters != null && parameters.get("l") != null) {
			learnCoef = parameters.get("l");
		}
	}

	/**
	 * Initialize the data structures required to built our opponent models.
	 * These mainly include the weights has maps containing issue weights (which issue is most important),
	 * issue value frequencies (how often is a certain issue value picked by the opponent), and
	 * utilities of opponent issue values (not the same as frequency as these are the normalized values per issue value).
	 */
	private void initializeOpponentModels() {
		// Number of opponents must be set before this function are called. Therefore call this function in setter.
		if(noOfOpponents < 1) {
			System.out.println("Number of opponents not set. Something's not right...");
		}
		// Initialize data structures to compute estimated utilities.
		opponentWeights = new HashMap<>();
		opponentFrequencies = new HashMap<>();
		opponentUtilities = new HashMap<>();
		// Get the issues from the negotiation session domain
		List<Issue> issues = negotiationSession.getDomain().getIssues();

		// Loop over opponents and create hash maps for weights, frequencies and utility.
		for(int opponentId = 0; opponentId < noOfOpponents; opponentId++) {
			// Add the opponent to the initial data structures that contain the data
			opponentWeights.put(opponentId, new HashMap<Issue, Double>());
			opponentFrequencies.put(opponentId, new HashMap<Issue, HashMap<Value, Double>>());
			opponentUtilities.put(opponentId, new HashMap<Issue, HashMap<Value, Double>>());

			// Loop over issues to initialize these in opponentWeights, Frequencies, and utilities
			for(Issue issue : issues) {
				if(!opponentWeights.get(opponentId).containsKey(issue)) {
					double initialWeight = 1D / (double) issues.size();
					opponentWeights.get(opponentId).put(issue, initialWeight);
					opponentFrequencies.get(opponentId).put(issue, new HashMap<Value, Double>());
					opponentUtilities.get(opponentId).put(issue, new HashMap<Value, Double>());
				}
			}
		}
	}

	/**
	 * Update our opponent model according to a new opponent bid made in the negotiations. Called on every new bid.
	 *
	 * @param opponentBid bid made by opponent
	 * @param time time in the negotiation, not being used.
	 */
	@Override
	public void updateModel(Bid opponentBid, double time) {
		if (noOfOpponents == -1) {
			return;
		}
		if (negotiationSession.getOpponentBidHistory().size() < noOfOpponents + 1) {
			return;
		}

		int opponentId = (negotiationSession.getOpponentBidHistory().size() - 1) % noOfOpponents;

		// Fetch current and previous bids from opponent.
		// TODO: Maybe we can fetch the opponent bid details in an easier way? Not sure if this actually gives us the correct bid details.
		BidDetails curOppBidDetails = negotiationSession.getOpponentBidHistory().getHistory()
				.get(negotiationSession.getOpponentBidHistory().size() - 1);
		BidDetails prevOppBidDetails = negotiationSession.getOpponentBidHistory().getHistory()
				.get(negotiationSession.getOpponentBidHistory().size() - (noOfOpponents + 1));

		// Update different components in opponent model such that we can compute the utility
		updateWeights(opponentId, opponentBid, curOppBidDetails, prevOppBidDetails);
		updateFrequencies(opponentId, opponentBid);
	}

	/**
	 * Update the issue weights to represent what is most important to the opponent.
	 *
	 * @param opponentId the opponent who made the current bid
	 * @param opponentBid the bid made by this opponent
	 * @param current bid details of opponent bid
	 * @param previous bid details of previous opponent bid
	 */
	private void updateWeights(int opponentId, Bid opponentBid, BidDetails current, BidDetails previous) {
		// For each issue, increment the weight of the the issue if it is the same as that of the previous bid.
		for (Issue issue : opponentBid.getIssues()) {
			// Get the current weight
			double issueWeight = opponentWeights.get(opponentId).get(issue);
			// Check if it is unchanged
			if(current.getBid().getValue(issue.getNumber()).equals(previous.getBid().getValue(issue.getNumber()))) {
				// If indeed unchanged, update weight with learning coefficient.
				opponentWeights.get(opponentId).put(issue, issueWeight + learnCoef);
			}
		}

		// Then normalize all weights such that sum equals 1.
		double total = 0;
		for (Issue issue : opponentWeights.get(opponentId).keySet()) {
			total += opponentWeights.get(opponentId).get(issue);
		}
		for (Issue issue : opponentWeights.get(opponentId).keySet()) {
			double newWeight = opponentWeights.get(opponentId).get(issue) / total;
			opponentWeights.get(opponentId).put(issue, newWeight);
		}
	}

	/**
	 * Update the frequencies of issue values for this opponent based on opponent bid.
	 *
	 * @param opponentId opponent who made the bid.
	 * @param opponentBid bid made by opponent.
	 */
	private void updateFrequencies(int opponentId, Bid opponentBid) {
		// Loop over issues and increment if some issue value was found before.
		for (Issue issue : opponentBid.getIssues()) {
			// Check if the value has been initialized earlier. If not, set default value of 0.
			if(!opponentFrequencies.get(opponentId).get(issue).containsKey(opponentBid.getValue(issue.getNumber()))) {
				opponentFrequencies.get(opponentId).get(issue).put(opponentBid.getValue(issue.getNumber()), 0D);
			}
			// Increment
			double issueFrequency = opponentFrequencies.get(opponentId).get(issue).get(opponentBid.getValue(issue.getNumber()));
			opponentFrequencies.get(opponentId).get(issue).put(opponentBid.getValue(issue.getNumber()), issueFrequency + 1);
		}

		// Then insert these frequencies into opponent utilities per issue value as normalized to max frequency.
		for (Issue issue : opponentFrequencies.get(opponentId).keySet()) {
			// Compute maximum frequency amongst issue values
			double max = -1;
			for(double frequency : opponentFrequencies.get(opponentId).get(issue).values()) {
				if(frequency > max) {
					max = frequency;
				}
			}
			// Set each value for this issue to the frequency divided by the maximum frequency in opponent utility
			for(Value val : opponentFrequencies.get(opponentId).get(issue).keySet()) {
				double utilityValue = opponentFrequencies.get(opponentId).get(issue).get(val) / max;
				opponentUtilities.get(opponentId).get(issue).put(val, utilityValue);
			}
		}
	}

	/**
	 * Evaluate a bid based on the estimated utilities of opponents, returning the average utility of all opponents.
	 *
	 * @param bid to estimate opponent utilities on.
	 * @return the average utilities of all opponents.
	 */
	@Override
	public double getBidEvaluation(Bid bid) {
		if(noOfOpponents == -1) {
			return -1;
		}
		// Set a total utility that estimates the average utility of all agents we play against for bid.
		double totalUtility = 0;
		// Loop over all opponents to estimate utility of opponent for this bid.
		for(int opponentId = 0; opponentId < noOfOpponents; opponentId++) {
			// Initialize to utility 0 and increment based on weight and frequency value.
			double oppUtility = 0;
			for(Issue issue : bid.getIssues()) {
				// If issue value is in utility mapping, increment with weight * issue value.
				if(opponentUtilities.get(opponentId).get(issue).containsKey(bid.getValue(issue.getNumber()))) {
					double issueValUtil = opponentUtilities.get(opponentId).get(issue).get(bid.getValue(issue.getNumber()));
					oppUtility += opponentWeights.get(opponentId).get(issue) * issueValUtil;
				}
				// Otherwise increment with 0 or an expected value ???
				else {
					// TODO: If not set for utils, what value to pick? 0.5 ??
					oppUtility += opponentWeights.get(opponentId).get(issue) * 0.5;
				}
			}
			totalUtility += oppUtility;
		}
		return totalUtility / noOfOpponents;
	}

	/**
	 * Get the number of opponents were playing against.
	 *
	 * @return number of opponents
	 */
	public int getNoOfOpponents() {
		return noOfOpponents;
	}

	/**
	 * Set the number of opponents in our opponent model. Necessary to build the different opponent models.
	 *
	 * @param noOfOpponents the number of opponents as set in the opponent model.
	 */
	public void setNoOfOpponents(int noOfOpponents) {
		this.noOfOpponents = noOfOpponents;
		initializeOpponentModels();
	}

	@Override
	public String getName() {
		return "Frequency Model as learned in class";
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("l", 0.2,
				"The learning coefficient determines how quickly the issue weights are learned"));
		return set;
	}
}
