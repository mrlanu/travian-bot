import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        run();
    }

    static void run() {
        try (Game game = new Game();
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
            while (true) {
                System.out.println("What do you like to do ?");
                System.out.println("Find all oases - 1");
                System.out.println("Find Elephants - 2");
                System.out.println("Exit - 3");
                int choice = Integer.parseInt(reader.readLine());
                if (choice == 1){
                    System.out.println("What is your village X coordinate ?");
                    int x = Integer.parseInt(reader.readLine());
                    System.out.println("What is your village Y coordinate ?");
                    int y = Integer.parseInt(reader.readLine());
                    System.out.println("What a radius do You like ?");
                    int r = Integer.parseInt(reader.readLine());
                    game.findAllOases(x, y, r);
                } else if (choice == 2){
                    game.checkForElephants();
                } else if (choice == 3){
                    break;
                }
            }
        } catch (Exception e){
            System.out.println("Error - " + e.getMessage());
        }
    }
}
