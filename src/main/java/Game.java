import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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
        this.userName = properties.getProperty("user.name");
        this.password = properties.getProperty("user.password");
        this.pathToFileAllOases = properties.getProperty("file.all-oases");
        this.pathToAllElephants = properties.getProperty("file.all-elephants");
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

        List<Oasis> oases = Util.readFromFile(pathToFileAllOases);

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

    public void tryAttack() throws IOException {

        /*HtmlTable table = (HtmlTable) confirmPage.getElementById("short_info");
        System.out.println(table);
        System.out.println("Cell (1,2)=" + table.getCellAt(1,2));
*/
        HtmlButton confButton = (HtmlButton) createWave("3", "-34", "78").getElementById("btn_ok");
        HtmlButton confButton2 = (HtmlButton) createWave("2", "-34", "78").getElementById("btn_ok");

        confButton.click();
        /*try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        confButton2.click();

        System.out.println("TextField =====>>>>>");
        System.out.println(confButton);
    }

    public HtmlPage createWave(String troops, String x, String y) throws IOException {
        URL url = new URL(String.format("%s/build.php?tt=2&gid=16",server));
        WebRequest requestSettings = new WebRequest(url, HttpMethod.POST);

        requestSettings.setRequestBody("redeployHero=&troops%5B0%5D%5Bt1%5D%3E=0&troops%5B0%5D%5Bt2%5D%3E=0&troops%5B0%5D%5Bt3%5D%3E=0&troops%5B0%5D%5Bt4%5D%3E=2&troops%5B0%5D%5Bt5%5D%3E=0&troops%5B0%5D%5Bt6%5D%3E=0&troops%5B0%5D%5Bt7%5D%3E=0&troops%5B0%5D%5Bt8%5D%3E=0&troops%5B0%5D%5Bt9%5D%3E=0&troops%5B0%5D%5Bt10%5D%3E=0&troops%5B0%5D%5Bt11%5D%3E=0&currentDid=18005&b=2&dname=&x=-34&y=78");

        Page redirectPage = webClient.getPage(requestSettings);
        System.out.println(redirectPage);
    /*HtmlPage pSPage = webClient.getPage(String.format("%s/build.php?tt=2&id=39",server));
        HtmlForm attackForm = pSPage.getFormByName("snd");
        HtmlButton buttonByName = attackForm.getButtonByName("s1");
        HtmlTextInput textField = attackForm.getInputByName("troops[0][t4]");
        HtmlTextInput textFieldX = attackForm.getInputByName("x");
        HtmlTextInput textFieldY = attackForm.getInputByName("y");
        HtmlRadioButtonInput radio = attackForm.getInputByName("c");
        radio.setDefaultValue("3");
        textFieldX.type(x);
        textFieldY.type(y);
        textField.type(troops);
        System.out.println(buttonByName.getDefaultValue());*/
        return null;//buttonByName.click();
    }

    @Override
    public void close() throws Exception {
        webClient.close();
    }
}
