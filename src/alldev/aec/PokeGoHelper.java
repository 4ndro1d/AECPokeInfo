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
	static int refreshTime = 20000;
	static AECInfoListener listener;

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
		if (!isAuthenticated)
			try {
				login();
			} catch (LoginFailedException | RemoteServerException | IOException e) {
				e.printStackTrace();
			}

		getAECGym();
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
		poGo = new PokemonGo(provider, httpClient);
	}

	private void loginPokeGo() {
		try {
			poGo = new PokemonGo(new GoogleUserCredentialProvider(httpClient,
					refreshToken), httpClient);
			isAuthenticated = true;
		} catch (LoginFailedException | RemoteServerException e) {
			e.printStackTrace();
		}
	}

	private void getAECGym() {
		if (poGo != null) {
			poGo.setLocation(48.3097431, 14.2821328, 0);
			try {
				List<Gym> gyms = poGo.getMap().getGyms();
				for (Gym gym : gyms) {
					if (gym.getName().equals(gymNameAEC)) {

						AECInfo info = new AECInfo();
						info.setInBattle(gym.getIsInBattle());
						info.setTeamColor(gym.getOwnedByTeam().toString());
						info.setGymLevel(gym.getGymMembers().size());

						if (listener != null)
							listener.AECInfoUpdated(info);

						break;
					}
				}
			} catch (LoginFailedException | RemoteServerException e) {
				e.printStackTrace();
			}
		}
	}

	public static void registerListener(AECInfoListener i) {
		listener = i;
	}
}
