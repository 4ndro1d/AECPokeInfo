package alldev.aec;

public class ListenTest implements AECInfoListener {

	public ListenTest() {
		PokeGoHelper.registerListener(this);
		PokeGoHelper.startAECInfoFetch();
	}

	@Override
	public void AECInfoUpdated(AECInfo arg0) {
		System.out.println("gym level: " + arg0.getGymLevel());
		System.out.println("team: " + arg0.getTeamColor());
	}
}
