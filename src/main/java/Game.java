import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Game implements AutoCloseable {

    private String userName;
    private String password;
    private WebClient webClient;
    private String server;
    private String pathToFileAllOases;
    private String pathToAllElephants;

    Game() throws IOException {
        initProperties();
        init();
    }

    private void initProperties() throws IOException {
        File file = new File("src/main/resources/application.properties");
        Properties properties = new Properties();
        properties.load(new FileReader(file));

        this.server = properties.getProperty("server");
        this.userName = properties.getProperty("userName");
        this.password = properties.getProperty("password");
        this.pathToFileAllOases = properties.getProperty("file.allOases");
        this.pathToAllElephants = properties.getProperty("file.allElephants");
    }

    private void init() throws IOException {
        this.webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        login();
    }

    void findAllOases(int myVillageX, int myVillageY, int length) throws IOException {
        System.out.println("Scanning all oases...");
        List<Oasis> result = new ArrayList<>();
        for (int y = myVillageY + length; y >= myVillageY - length; y = y - 7){
            for (int x = myVillageX - length; x <= myVillageX + length; x = x + 10){
                HtmlPage detailPage = webClient.getPage(String.format("%s/karte.php?x=%d&y=%d",server, x, y));
                List<HtmlElement> tileRow = detailPage.getByXPath("//div[@class='mapContainerData']//div[@class='tileRow']//div");
                tileRow.forEach(t -> {
                    String s = t.getAttribute("class");
                    if (s.contains("oasis")){
                        String[] str = s.split(" ");
                        int xOne = Integer.parseInt(str[2].split("\\{|\\}")[1]);
                        int yOne = Integer.parseInt(str[3].split("\\{|\\}")[1]);
                        result.add(new Oasis(null, xOne, yOne, false));
                    }
                });
            }
        }
        Util.writeToFile(result, pathToFileAllOases);
        System.out.println("Oases found - " + result.size());
        System.out.println("--------------------------------------------");
    }

    void checkForElephants() {

        System.out.println("Searching all elephants...");

        List<Oasis> oases = Util.readFromFile();

        oases.forEach(oasis -> {
            try {
                HtmlPage detailPage = webClient.getPage(String.format("%s/position_details.php?x=%d&y=%d",server, oasis.getX(), oasis.getY()));
                List<HtmlElement> tileRow = detailPage.getByXPath("//table[@id='troop_info']//img[@alt='Слон']");
                if (tileRow.size() > 0){
                    oasis.setHasElephants(true);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        List<Oasis> filteredResult = oases.stream()
                .filter(Oasis::isHasElephants)
                .collect(Collectors.toList());

        Util.writeToFile(filteredResult, pathToAllElephants);
        System.out.println("Oases with elephants - " + filteredResult.size());
        System.out.println("Complete. Check the " + pathToAllElephants);
        System.out.println("--------------------------------------------");
    }

    private void login() throws IOException {

        HtmlPage startPage = webClient.getPage(server + "/dorf1.php");
        HtmlForm loginForm = startPage.getFormByName("login");
        HtmlButton button = loginForm.getButtonByName("s1");
        HtmlTextInput textField = loginForm.getInputByName("name");
        HtmlCheckBoxInput checkBoxInput = loginForm.getInputByName("lowRes");
        checkBoxInput.setChecked(true);
        HtmlPasswordInput textFieldPass = loginForm.getInputByName("password");
        textField.type(userName);
        textFieldPass.type(password);

        //Village Page
        HtmlPage currentPage = button.click();
        HtmlAnchor heroName = (HtmlAnchor) currentPage.getByXPath("//div[@class='playerName']//a[@href='spieler.php']").get(1);
        System.out.println(String.format("Hello - %s", heroName.asText()));
    }

    @Override
    public void close() throws Exception {
        webClient.close();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
