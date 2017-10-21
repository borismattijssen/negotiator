package ai2017.group1.boa.opponent;

import agents.bayesianopponentmodel.BayesianOpponentModel;
import agents.bayesianopponentmodel.OpponentModelUtilSpace;
import negotiator.Bid;
import negotiator.boaframework.BOAparameter;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.issue.Issue;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BayesianModel extends OpponentModel {


	private int startingBidIssue = 0;
	private int noOfOpponents = -1;
	private BayesianOpponentModel[] models;

	public BayesianModel() {
	}

	public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
		this.negotiationSession = negotiationSession;

		while(!this.testIndexOfFirstIssue(negotiationSession.getUtilitySpace().getDomain().getRandomBid((Random)null), this.startingBidIssue)) {
			++this.startingBidIssue;
		}

	}

	private void initializeOpponentModels() {
		models = new BayesianOpponentModel[noOfOpponents];

		for (int i = 0; i < noOfOpponents; i++) {
			models[i] = new BayesianOpponentModel( (AdditiveUtilitySpace) negotiationSession.getUtilitySpace());
			models[i].setMostProbableUSHypsOnly(false); // default value
		}
	}

	private boolean testIndexOfFirstIssue(Bid bid, int i) {
		try {
			ValueDiscrete e = (ValueDiscrete)bid.getValue(i);
			return true;
		} catch (Exception var4) {
			return false;
		}
	}

	public void updateModel(Bid opponentBid, double time) {
		if (noOfOpponents == -1) {
			return;
		}
		if (negotiationSession.getOpponentBidHistory().size() < noOfOpponents + 1) {
			return;
		}

		int oppoNo = (negotiationSession.getOpponentBidHistory().size() - 1) % noOfOpponents;

		try {
			this.models[oppoNo].updateBeliefs(opponentBid);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public double getBidEvaluation(Bid bid) {
		if (noOfOpponents == -1) {
			return 0;
		}
		double total = 0;

		try {
			for (int i = 0; i < noOfOpponents; i++) {
				total += models[i].getNormalizedUtility(bid);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return 0.0;
		}
		return total / noOfOpponents;
	}

	public int getNoOfOpponents() {
		return noOfOpponents;
	}

	public void setNoOfOpponents(int noOfOpponents) {
		this.noOfOpponents = noOfOpponents;
		initializeOpponentModels();
	}

//	public double getWeight(Issue issue) {
//		return this.models.getNormalizedWeight(issue, this.startingBidIssue);
//	}

	public AdditiveUtilitySpace getOpponentUtilitySpace(int opponent) {
		return new OpponentModelUtilSpace(this.models[opponent]);
	}

	public void cleanUp() {
		super.cleanUp();
	}

	public String getName() {
		return "Bayesian Model";
	}

	public Set<BOAparameter> getParameterSpec() {
		HashSet set = new HashSet();
		set.add(new BOAparameter("m", Double.valueOf(0.0D), "If higher than 0 the most probable hypothesis is only used"));
		return set;
	}

}
