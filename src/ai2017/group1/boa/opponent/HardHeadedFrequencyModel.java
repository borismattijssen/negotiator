package ai2017.group1.boa.opponent;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import joptsimple.internal.Strings;
import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.BOAparameter;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

/**
 * BOA framework implementation of the HardHeaded Frequecy Model. My main
 * contribution to this model is that I fixed a bug in the mainbranch which
 * resulted in an equal preference of each bid in the ANAC 2011 competition.
 * Effectively, the corrupt model resulted in the offering of a random bid in
 * the ANAC 2011.
 * 
 * Default: learning coef l = 0.2; learnValueAddition v = 1.0
 * 
 * Adapted by Mark Hendrikx to be compatible with the BOA framework.
 * 
 * Tim Baarslag, Koen Hindriks, Mark Hendrikx, Alex Dirkzwager and Catholijn M.
 * Jonker. Decoupling Negotiating Agents to Explore the Space of Negotiation
 * Strategies
 * 
 */
public class HardHeadedFrequencyModel extends OpponentModel {

	// the learning coefficient is the weight that is added each turn to the
	// issue weights
	// which changed. It's a trade-off between concession speed and accuracy.
	private double learnCoef;
	// value which is added to a value if it is found. Determines how fast
	// the value weights converge.
	private int learnValueAddition;

	private int amountOfIssues;
	private int noOfOpponents = -1;

	private AdditiveUtilitySpace[] opponentUtilitySpaces;

	private String logFile;

	/**
	 * Initializes the utility space of the opponent such that all value issue
	 * weights are equal.
	 */
	@Override
	public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
		super.init(negotiationSession, parameters);
		this.negotiationSession = negotiationSession;
		if (parameters != null && parameters.get("l") != null) {
			learnCoef = parameters.get("l");
		} else {
			learnCoef = 0.2;
		}
		learnValueAddition = 1;
		amountOfIssues = negotiationSession.getDomain().getIssues().size();
		System.out.println(amountOfIssues);
	}

	private void initializeUtilitySpaces() {
		Map<Objective, Evaluator> fEvaluators = new HashMap();
		List<Issue> issues = negotiationSession.getDomain().getIssues();
		for(Issue issue : issues) {
			fEvaluators.put(issue, new EvaluatorDiscrete());
		}

		opponentUtilitySpaces = new AdditiveUtilitySpace[noOfOpponents];
		for (int i = 0; i < noOfOpponents; i++) {
			try {
				opponentUtilitySpaces[i] = new AdditiveUtilitySpace(negotiationSession.getDomain(), fEvaluators);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// @TODO kinda redundant, could fix this
			double commonWeight = 1D / (double) amountOfIssues;

			// initialize the weights
			for (Entry<Objective, Evaluator> e : opponentUtilitySpaces[i].getEvaluators()) {
				// set the issue weights
				opponentUtilitySpaces[i].unlock(e.getKey());
				e.getValue().setWeight(commonWeight);
				try {
					// set all value weights to one (they are normalized when
					// calculating the utility)
					for (ValueDiscrete vd : ((IssueDiscrete) e.getKey()).getValues())
						((EvaluatorDiscrete) e.getValue()).setEvaluation(vd, 1);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Determines the difference between bids. For each issue, it is determined
	 * if the value changed. If this is the case, a 1 is stored in a hashmap for
	 * that issue, else a 0.
	 * 
	 * @return diff
	 */
	private HashMap<Integer, Integer> determineDifference(BidDetails first, BidDetails second) {

		HashMap<Integer, Integer> diff = new HashMap<Integer, Integer>();
		try {
			for (Issue i : negotiationSession.getDomain().getIssues()) {
				diff.put(i.getNumber(), (((ValueDiscrete) first.getBid().getValue(i.getNumber()))
						.equals((ValueDiscrete) second.getBid().getValue(i.getNumber()))) ? 0 : 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return diff;
	}

	/**
	 * Updates the opponent model given a bid.
	 */
	@Override
	public void updateModel(Bid opponentBid, double time) {
		if (noOfOpponents == -1) {
			return;
		}
		if (negotiationSession.getOpponentBidHistory().size() < noOfOpponents + 1) {
			return;
		}
		int numberOfUnchanged = 0;
		int oppoNo = (negotiationSession.getOpponentBidHistory().size() - 1) % noOfOpponents;

		BidDetails oppBid = negotiationSession.getOpponentBidHistory().getHistory()
				.get(negotiationSession.getOpponentBidHistory().size() - 1);
		BidDetails prevOppBid = negotiationSession.getOpponentBidHistory().getHistory()
				.get(negotiationSession.getOpponentBidHistory().size() - (noOfOpponents + 1));
		HashMap<Integer, Integer> lastDiffSet = determineDifference(prevOppBid, oppBid);

		// count the number of changes in value
		for (Integer i : lastDiffSet.keySet()) {
			if (lastDiffSet.get(i) == 0)
				numberOfUnchanged++;
		}
		
		double newTotal = 1D + (learnCoef * (double) numberOfUnchanged);

		// re-weighing issues while making sure that the sum remains 1
		for (Integer i : lastDiffSet.keySet()) {
			if (lastDiffSet.get(i) == 0)
				opponentUtilitySpaces[oppoNo].setWeight(opponentUtilitySpaces[oppoNo].getDomain().getObjectives().get(i),
						(opponentUtilitySpaces[oppoNo].getWeight(i) + learnCoef) / newTotal);
			else
				opponentUtilitySpaces[oppoNo].setWeight(opponentUtilitySpaces[oppoNo].getDomain().getObjectives().get(i),
						opponentUtilitySpaces[oppoNo].getWeight(i) / newTotal);
		}


		// Then for each issue value that has been offered last time, a constant
		// value is added to its corresponding ValueDiscrete.
		try {
			for (Entry<Objective, Evaluator> e : opponentUtilitySpaces[oppoNo].getEvaluators()) {
				// cast issue to discrete and retrieve value. Next, add constant
				// learnValueAddition to the current preference of the value to
				// make
				// it more important
				((EvaluatorDiscrete) e.getValue()).setEvaluation(
						oppBid.getBid().getValue(((IssueDiscrete) e.getKey()).getNumber()),
						(learnValueAddition + ((EvaluatorDiscrete) e.getValue()).getEvaluationNotNormalized(
								((ValueDiscrete) oppBid.getBid().getValue(((IssueDiscrete) e.getKey()).getNumber())))));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public double getBidEvaluation(Bid bid) {
	    if (noOfOpponents == -1) {
	    	return 0;
		}
		double total = 0;
		try {
			for (int i = 0; i < noOfOpponents; i++) {
				total += opponentUtilitySpaces[i].getUtility(bid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total/noOfOpponents;
	}

	@Override
	public String getName() {
		return "HardHeaded Frequency Model example";
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("l", 0.2,
				"The learning coefficient determines how quickly the issue weights are learned"));
		return set;
	}

	public int getNoOfOpponents() {
		return noOfOpponents;
	}

	public void setNoOfOpponents(int noOfOpponents) {
		this.noOfOpponents = noOfOpponents;
		initializeUtilitySpaces();
	}
}