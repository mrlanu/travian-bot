import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Game implements AutoCloseable {

    private String userName;
    private String password;
    private WebClient webClient;
    private String server;
    private String pathToFileAllOases;
    private String pathToAllElephants;
    boolean needC = true;

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

        HtmlButton confButton = (HtmlButton) createWave("3", "-35", "79").getElementById("btn_ok");
        System.out.println(confButton.getTextContent());
        confButton.click();
        /*HtmlButton confButton2 = (HtmlButton) createWave("2", "-34", "78").getElementById("btn_ok");

        confButton.click();
        *//*try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*//*
        confButton2.click();

        System.out.println("TextField =====>>>>>");
        System.out.println(confButton);*/
    }

    private HtmlPage createWave(String troops, String x, String y) throws IOException {

        //setup initial values for attack
        String result;
        StringBuilder sParams = new StringBuilder();
        HtmlPage pSPage = webClient.getPage(String.format("%s/build.php?tt=2&id=39",server));
        HtmlForm attackForm = pSPage.getFormByName("snd");
        HtmlTextInput textField = attackForm.getInputByName("troops[0][t5]");
        HtmlTextInput textFieldX = attackForm.getInputByName("x");
        HtmlTextInput textFieldY = attackForm.getInputByName("y");
        HtmlRadioButtonInput radio = attackForm.getInputByName("c");
        radio.setDefaultValue("3");
        textFieldX.type(x);
        textFieldY.type(y);
        textField.type(troops);

        //create parameters for request
        attackForm.getElementsByTagName("input")
                .stream()
                .map(i -> (HtmlInput) i)
                .peek(input -> {
                    String inputName = input.getNameAttribute();
                    if(inputName.equals("redeployHero")) {
                        sParams.append("redeployHero=&");
                    }else if (inputName.matches("^t\\d") || inputName.matches("x|y")){
                        sParams.append(inputName + "=" + input.getValueAttribute() + "&");
                    }else if (inputName.equals("c")) {

                    }else {
                        sParams.append(inputName + "=" + input.getValueAttribute() + "&");
                    }
                })
                .filter(i -> i.getNameAttribute().equals("c") && i.isChecked())
                .forEach(i2 -> sParams.append("c=" + i2.getValueAttribute() + "&"));

        result = sParams.toString();

        System.out.println(result.substring(0, result.length() - 1));

        //get Cookie
        URL url = new URL(String.format("%s/build.php?tt=2&id=39",server));
        Set<Cookie> cookieSet = webClient.getCookies(url);
        StringBuilder cB = new StringBuilder();
        cookieSet.forEach(cookie -> cB.append(cookie));
        String cookie = cB.toString();

        //send async request via HttpClient
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(result))
                .uri(URI.create(String.format("%s/build.php?tt=2&id=39",server)))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .header("Cookie", cookie)
                .build();

        try {
            CompletableFuture<HttpResponse<String>> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            TimeUnit.MILLISECONDS.sleep(200);
            CompletableFuture<HttpResponse<String>> response2 = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Request has been sent.");
            String body = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
            String body2 = response2.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

            print(body);
            System.out.println("----------------------");
            print(body2);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        return null;//redirectPage;
    }

    private void print(String body){
        try (BufferedReader reader = new BufferedReader(new StringReader(body))){
            StringBuilder stringBuilder = new StringBuilder();
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                if (nextLine.contains("<input") || (nextLine.contains("btn_ok") && nextLine.contains("<button"))){
                    System.out.println(nextLine);
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void close() throws Exception {
        webClient.close();
    }
}
