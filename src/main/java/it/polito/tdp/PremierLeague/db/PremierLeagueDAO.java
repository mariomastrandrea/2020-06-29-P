package it.polito.tdp.PremierLeague.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import it.polito.tdp.PremierLeague.model.Action;
import it.polito.tdp.PremierLeague.model.Match;
import it.polito.tdp.PremierLeague.model.MatchesPair;
import it.polito.tdp.PremierLeague.model.Player;

public class PremierLeagueDAO 
{	
	public List<Player> listAllPlayers(){
		String sql = "SELECT * FROM Players";
		List<Player> result = new ArrayList<Player>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Player player = new Player(res.getInt("PlayerID"), res.getString("Name"));
				
				result.add(player);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> listAllActions(){
		String sql = "SELECT * FROM Actions";
		List<Action> result = new ArrayList<Action>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Action action = new Action(res.getInt("PlayerID"),res.getInt("MatchID"),res.getInt("TeamID"),res.getInt("Starts"),res.getInt("Goals"),
						res.getInt("TimePlayed"),res.getInt("RedCards"),res.getInt("YellowCards"),res.getInt("TotalSuccessfulPassesAll"),res.getInt("totalUnsuccessfulPassesAll"),
						res.getInt("Assists"),res.getInt("TotalFoulsConceded"),res.getInt("Offsides"));
				
				result.add(action);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Match> listAllMatches(){
		String sql = "SELECT m.MatchID, m.TeamHomeID, m.TeamAwayID, m.teamHomeFormation, m.teamAwayFormation, m.resultOfTeamHome, m.date, t1.Name, t2.Name   "
				+ "FROM Matches m, Teams t1, Teams t2 "
				+ "WHERE m.TeamHomeID = t1.TeamID AND m.TeamAwayID = t2.TeamID";
		List<Match> result = new ArrayList<Match>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				
				Match match = new Match(res.getInt("m.MatchID"), res.getInt("m.TeamHomeID"), res.getInt("m.TeamAwayID"), res.getInt("m.teamHomeFormation"), 
							res.getInt("m.teamAwayFormation"),res.getInt("m.resultOfTeamHome"), res.getTimestamp("m.date").toLocalDateTime(), res.getString("t1.Name"),res.getString("t2.Name"));
				
				
				result.add(match);

			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Month> getAllMonths()
	{
		final String querySql = "SELECT DISTINCT MONTH(Date) AS mon FROM Matches ORDER BY mon ASC";
		
		List<Month> allMonths = new ArrayList<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(querySql);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				int numMonth = queryResult.getInt("mon");
				Month month = Month.of(numMonth);
				allMonths.add(month);
			}
			queryResult.close();
			statement.close();
			connection.close();
			return allMonths;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllMonths()", sqle);
		}
	}

	public Collection<Match> getMatches(Month selectedMonth, Map<Integer, Match> matchesIdMap)
	{
		final String querySql = String.format("%s %s %s",
							"SELECT m.MatchID, m.TeamHomeID, m.TeamAwayID, m.teamHomeFormation, m.teamAwayFormation, m.resultOfTeamHome, m.date, t1.Name, t2.Name",
							"FROM Matches m, Teams t1, Teams t2",
							"WHERE MONTH(Date) = ? AND m.TeamHomeID = t1.TeamID AND m.TeamAwayID = t2.TeamID");
		
		Collection<Match> matches = new HashSet<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(querySql);
			statement.setInt(1, selectedMonth.getValue());
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				int matchId = queryResult.getInt("m.MatchID");
				
				if(!matchesIdMap.containsKey(matchId))
				{
					Match match = new Match(matchId, queryResult.getInt("m.TeamHomeID"), 
							queryResult.getInt("m.TeamAwayID"), queryResult.getInt("m.teamHomeFormation"), 
							queryResult.getInt("m.teamAwayFormation"), queryResult.getInt("m.resultOfTeamHome"), 
							queryResult.getTimestamp("m.date").toLocalDateTime(), queryResult.getString("t1.Name"),
							queryResult.getString("t2.Name"));
					
					matchesIdMap.put(matchId, match);
				}
				
				matches.add(matchesIdMap.get(matchId));
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return matches;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getMatches()", sqle);
		}
	}

	public Collection<MatchesPair> getMatchesPairs(Month selectedMonth, 
			int minMinutes, Map<Integer, Match> matchesIdMap)
	{
		final String querySql = String.format("%s %s %s %s %s %s",
				"SELECT m1.MatchID AS id1, m2.MatchID AS id2, COUNT(DISTINCT a1.PlayerID) AS numPlayers",
				"FROM Matches m1, Matches m2, Actions a1, Actions a2",
				"WHERE m1.MatchID = a1.MatchID AND MONTH(m1.Date) = ? AND a1.TimePlayed >= ?",
					"AND m2.MatchID = a2.MatchID AND MONTH(m2.Date) = ? AND a2.TimePlayed >= ?",
					"AND a1.PlayerID = a2.PlayerID AND m1.MatchID < m2.MatchID",
				"GROUP BY m1.MatchID, m2.MatchID");

		Collection<MatchesPair> matchesPairs = new HashSet<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(querySql);
			statement.setInt(1, selectedMonth.getValue());
			statement.setInt(2, minMinutes);
			statement.setInt(3, selectedMonth.getValue());
			statement.setInt(4, minMinutes);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				int matchID1 = queryResult.getInt("id1");
				int matchID2 = queryResult.getInt("id2");
				int numPlayersInCommon = queryResult.getInt("numPlayers");
				
				if(!matchesIdMap.containsKey(matchID1) ||
						!matchesIdMap.containsKey(matchID2))
					throw new RuntimeException("match id not found in idMap");
				
				Match match1 = matchesIdMap.get(matchID1);
				Match match2 = matchesIdMap.get(matchID2);

				MatchesPair pair = new MatchesPair(match1, match2, numPlayersInCommon);
				matchesPairs.add(pair);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return matchesPairs;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getMatchesPairs()", sqle);
		}
	}
	
}
