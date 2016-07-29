package alldev.aec;

public class ListenTest implements AECInfoListener {

	public ListenTest() {
		// PokeGoHelper.registerListener(this);
		// PokeGoHelper.startAECInfoFetch();
		AECPokeInfo info = new AECPokeInfo();
		info.registerListener(this);
		info.startRoutine();
	}

	@Override
	public void AECInfoUpdated(AECInfo arg0) {
		if (arg0.getExc() != null) {
			System.out.println("exception. server down?");
		} else {
			System.out.println("gym level: " + arg0.getGymLevel());
			System.out.println("team: " + arg0.getTeamColor());
			System.out.println("inBattle: " + arg0.isInBattle());
		}
	}
}
