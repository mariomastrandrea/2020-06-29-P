package it.polito.tdp.PremierLeague.model;

import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model 
{
	private final PremierLeagueDAO dao;
	private Graph<Match, DefaultWeightedEdge> graph;
	private final Map<Integer, Match> matchesIdMap;
	private List<Month> allMonths;
	
	private Collection<List<Match>> bestPaths;
	private int maxWeight;
	
	
	public Model()
	{
		this.dao = new PremierLeagueDAO();
		this.matchesIdMap = new HashMap<>();
		this.bestPaths = new HashSet<>();
	}
	
	public List<Month> getAllMonths()
	{
		if(this.allMonths == null)
			this.allMonths = this.dao.getAllMonths();
		
		return this.allMonths;
	}


	public void createGraph(int minMinutes, Month selectedMonth)
	{
		this.graph = GraphTypeBuilder.<Match, DefaultWeightedEdge>undirected()
									.allowingMultipleEdges(false)
									.allowingSelfLoops(false)
									.weighted(true)
									.edgeClass(DefaultWeightedEdge.class)
									.buildGraph();
		
		//add vertices
		Collection<Match> vertices = this.dao.getMatches(selectedMonth, this.matchesIdMap);
		Graphs.addAllVertices(this.graph, vertices);
		
		//add edges
		Collection<MatchesPair> matchesPairs = this.dao.getMatchesPairs(selectedMonth, 
														minMinutes, this.matchesIdMap);
		for(var pair : matchesPairs)
		{
			Match match1 = pair.getMatch1();
			Match match2 = pair.getMatch2();
			int weight = pair.getNumPlayersInCommon();
			
			Graphs.addEdge(this.graph, match1, match2, (double)weight);
		}
	}

	public int getNumVertices() { return this.graph.vertexSet().size(); }
	public int getNumEdges() { return this.graph.edgeSet().size(); }
	public boolean isGraphCreated() { return this.graph != null; }

	public Collection<MatchesPair> getMaxPlayersMatchesPairs()
	{
		int maxWeight = Integer.MIN_VALUE;
		Collection<DefaultWeightedEdge> maxWeightEdges = new HashSet<>();
		
		for(var edge : this.graph.edgeSet())
		{
			int weight = (int)this.graph.getEdgeWeight(edge);
			
			if(weight >= maxWeight)
			{
				if(weight > maxWeight)
				{
					maxWeight = weight;
					maxWeightEdges = new HashSet<>();
				}
				
				maxWeightEdges.add(edge);
			}
		}
		
		Collection<MatchesPair> maxPlayersMatchesPairs = new HashSet<>();
		
		for(var edge : maxWeightEdges)
		{
			Match match1 = this.graph.getEdgeSource(edge);
			Match match2 = this.graph.getEdgeTarget(edge);
			int numPlayersInCommon = (int)this.graph.getEdgeWeight(edge);
			
			MatchesPair newPair = new MatchesPair(match1, match2, numPlayersInCommon);
			maxPlayersMatchesPairs.add(newPair);
		}
		
		return maxPlayersMatchesPairs;
	}

	public List<Match> getOrderedMatches()
	{
		List<Match> orderedMatches = new ArrayList<>(this.graph.vertexSet());
		orderedMatches.sort((m1, m2) -> Integer.compare(m1.getMatchID(), m2.getMatchID()));
		
		return orderedMatches;
	}

	public Collection<List<Match>> getBestPathsBetween(Match start, Match destination)
	{
		this.bestPaths.clear();
		this.maxWeight = Integer.MIN_VALUE;
		
		List<Match> partialSolution = new ArrayList<>();
		partialSolution.add(start);
		
		Set<Match> partialSolutionSet = new HashSet<>();	//overhead to increase performance in .remove()
		partialSolutionSet.add(start);
		
		int currentWeight = 0;
		
		this.recursiveBestPathComputation(partialSolution, partialSolutionSet, 
														currentWeight, destination);
		return this.bestPaths;
	}

	private void recursiveBestPathComputation(List<Match> partialSolution, 
			Set<Match> partialSolutionSet, int currentWeight, Match destination)
	{
		Match lastAddedMatch = partialSolution.get(partialSolution.size() - 1);
		
		if(lastAddedMatch.equals(destination)) //terminal case
		{
			if(currentWeight >= this.maxWeight)
			{
				if(currentWeight > this.maxWeight)
				{
					this.maxWeight = currentWeight;
					this.bestPaths.clear();
				}
				
				List<Match> newBestPath = new ArrayList<>(partialSolution);
				this.bestPaths.add(newBestPath);
			}
			
			return; //stop recursion
		}
		
		for(var nextEdge : this.graph.edgesOf(lastAddedMatch))
		{
			int weight = (int)this.graph.getEdgeWeight(nextEdge);
			Match nextMatch = Graphs.getOppositeVertex(this.graph, nextEdge, lastAddedMatch);
			
			if(partialSolutionSet.contains(nextMatch) || this.haveSameTeams(lastAddedMatch, nextMatch))
				continue;	//filtering branches
			
			partialSolution.add(nextMatch);
			partialSolutionSet.add(nextMatch);
			//recursive call
			this.recursiveBestPathComputation(partialSolution, partialSolutionSet, 
													currentWeight + weight, destination);
			
			partialSolution.remove(partialSolution.size() - 1); //backtracking
			partialSolutionSet.remove(nextMatch);
		}
	}

	private boolean haveSameTeams(Match lastAddedMatch, Match nextMatch)
	{
		int match1Team1 = lastAddedMatch.teamHomeID;
		int match1Team2 = lastAddedMatch.teamAwayID;
		
		int match2Team1 = nextMatch.teamHomeID;
		int match2Team2 = nextMatch.teamAwayID;
		
		return (match1Team1 == match2Team1 && match1Team2 == match2Team2) 	//A vs B - A vs B
			|| (match1Team1 == match2Team2 && match1Team2 == match2Team1); 	//A vs B - B vs A 		
	}
}
