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

public class AECPokeInfo {

	private OkHttpClient httpClient = new OkHttpClient();
	private String refreshToken;
	private Path path = FileSystems.getDefault()
			.getPath("", "refreshToken.txt");

	private PokemonGo poGo;
	private boolean isAuthenticated = false;
	private String gymNameAEC = "Ars Electronica Center";
	private AECInfoListener listener;

	private Gym aec;

	public void startRoutine() {
		while (true) {
			try {
				if (!isAuthenticated)
					login();
				getAECGym();
				if (aec != null)
					broadcastInfo(getInfoFromGym(aec));
			} catch (Exception e) {
				AECInfo i = new AECInfo();
				i.setExc(new AECException());
				broadcastInfo(i);
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void login() throws LoginFailedException, RemoteServerException,
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

		poGo = new PokemonGo(provider, httpClient);
	}

	private void loginPokeGo() throws LoginFailedException,
			RemoteServerException {
		poGo = new PokemonGo(new GoogleUserCredentialProvider(httpClient,
				refreshToken), httpClient);
		isAuthenticated = true;

	}

	private void getAECGym() throws LoginFailedException,
			RemoteServerException, InterruptedException {

		if (poGo != null) {
			poGo.setLocation(48.3097431, 14.2821328, 0);

			List<Gym> gyms = poGo.getMap().getGyms();
			if (gyms.size() > 18)
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
		info.setTeamColor(gym.getOwnedByTeam().toString());
		Thread.sleep(10);
		info.setGymLevel(gym.getGymMembers().size());
		return info;
	}
}
