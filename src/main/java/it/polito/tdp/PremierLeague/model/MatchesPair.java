package it.polito.tdp.PremierLeague.model;

public class MatchesPair
{
	private final Match match1;
	private final Match match2;
	private final int numPlayersInCommon;
	
	
	public MatchesPair(Match match1, Match match2, int numPlayersInCommon)
	{
		this.match1 = match1;
		this.match2 = match2;
		this.numPlayersInCommon = numPlayersInCommon;
	}

	public Match getMatch1()
	{
		return this.match1;
	}

	public Match getMatch2()
	{
		return this.match2;
	}

	public int getNumPlayersInCommon()
	{
		return this.numPlayersInCommon;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((match1 == null) ? 0 : match1.hashCode());
		result = prime * result + ((match2 == null) ? 0 : match2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchesPair other = (MatchesPair) obj;
		if (match1 == null)
		{
			if (other.match1 != null)
				return false;
		}
		else
			if (!match1.equals(other.match1))
				return false;
		if (match2 == null)
		{
			if (other.match2 != null)
				return false;
		}
		else
			if (!match2.equals(other.match2))
				return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return String.format("- %s\n- %s\nGiocatori in comune: %d", 
				this.match1.toString(), this.match2.toString(), this.numPlayersInCommon);
	}
}
