package alldev.aec;

public class AECInfo {
	
	private boolean isInBattle;
	private String teamColor;
	private int gymLevel;
	
	public int getGymLevel() {
		return gymLevel;
	}
	public void setGymLevel(int gymLevel) {
		this.gymLevel = gymLevel;
	}
	public boolean isInBattle() {
		return isInBattle;
	}
	public void setInBattle(boolean isInBattle) {
		this.isInBattle = isInBattle;
	}
	public String getTeamColor() {
		return teamColor;
	}
	public void setTeamColor(String teamColor) {
		this.teamColor = teamColor;
	}

}
