package helpers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by alex on 2/19/2018.
 */
public class SearchIn {
    private ArrayList<String> file = new ArrayList<>();

    public SearchIn(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();

            while (line != null) {
                file.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
        }
    }


    public ArrayList<String> getLinesByText(String textForSearching) {
        ArrayList<String> lines = new ArrayList<>();
        for (int k = 0; k < file.size(); k++) {
            String line = file.get(k);
            if (line.contains(textForSearching)) {
                lines.add(line);
            }
        }
        if (lines.size() < 1) {
            System.out.println("There is no machted text for: " + textForSearching);
        }
        return lines;
    }
}
