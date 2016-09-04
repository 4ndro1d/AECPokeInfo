package alldev.aec;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import okhttp3.OkHttpClient;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

public class PokeGoHelper implements Runnable {

	OkHttpClient httpClient = new OkHttpClient();
	String refreshToken;
	Path path = FileSystems.getDefault().getPath("", "refreshToken.txt");

	private static PokemonGo poGo;
	private static boolean isAuthenticated = false;

	static String gymNameAEC = "Ars Electronica Center";
	static int refreshTime = 2000;
	static AECInfoListener listener;
	private Gym aec;

	static public void main(String[] args) {
		new ListenTest();
	}

	public static void startAECInfoFetch() {
		while (true) {
			(new Thread(new PokeGoHelper())).start();
			try {
				Thread.sleep(refreshTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		try {
			if (!isAuthenticated)
				login();
			getAECGym();
			if (aec != null)
				broadcastInfo(getInfoFromGym(aec));
		} catch (LoginFailedException | RemoteServerException | IOException
				| InterruptedException e) {
			AECInfo i = new AECInfo();
			i.setExc(new AECException());
			broadcastInfo(i);
		}

	}

	private void login() throws LoginFailedException, RemoteServerException,
			IOException {
		try {
			if (path.toFile().exists())
				readToken();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		if (refreshToken == null) {
			loginPokeGoFirstTime();
		} else {
			loginPokeGo();
		}
	}

	private void readToken() throws IOException, ClassNotFoundException {
		refreshToken = Files.readAllLines(path).get(0);
	}

	private void saveToken(String token) throws IOException {
		Files.write(path, token.getBytes());
	}

	private void loginPokeGoFirstTime() throws LoginFailedException,
			RemoteServerException, IOException {
		GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(
				httpClient);

		System.out.println("Please go to "
				+ GoogleUserCredentialProvider.LOGIN_URL);
		System.out.println("Enter authorisation code:");

		Scanner sc = new Scanner(System.in);
		String access = sc.nextLine();
		sc.close();

		provider.login(access);

		saveToken(provider.getRefreshToken());
		isAuthenticated = true;
		poGo = new PokemonGo(httpClient);
		poGo.login(provider);
	}

	private void loginPokeGo() {
		try {
			poGo = new PokemonGo(httpClient);
			poGo.login(new GoogleUserCredentialProvider(httpClient,
					refreshToken));
			isAuthenticated = true;
		} catch (LoginFailedException | RemoteServerException e) {
			e.printStackTrace();
		}
	}

	public void registerListener(AECInfoListener i) {
		listener = i;
	}

	void broadcastInfo(AECInfo info) {
		if (listener != null)
			listener.AECInfoUpdated(info);
	}

	private AECInfo getInfoFromGym(Gym gym) throws LoginFailedException,
			RemoteServerException, InterruptedException {
		AECInfo info = new AECInfo();
		info.setInBattle(gym.getIsInBattle());
		Thread.sleep(10);
	//	info.setTeamColor(gym.getOwnedByTeam().toString());
		Thread.sleep(10);
		info.setGymLevel(gym.getGymMembers().size());
		return info;
	}

	private void getAECGym() throws LoginFailedException,
			RemoteServerException, InterruptedException {

		if (poGo != null) {
			poGo.setLocation(48.3097431, 14.2821328, 0);

			List<Gym> gyms = poGo.getMap().getGyms();
			aec = gyms.get(18);
			if (!aec.getName().equals(gymNameAEC)) {
				for (int i = 0; i < gyms.size(); i++) {
					Gym gym = gyms.get(i);
					String name = gym.getName();

					if (name.equals(gymNameAEC)) {
						aec = gym;
						break;
					}
					Thread.sleep(500);
				}
			}
		}
	}

}
