package jnetention.util;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jnetention.Core;
import jnetention.NProperty;
import jnetention.NTag;
import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;


/**
 *
 * @author me
 */
public class SchemaOrg {

    public SchemaOrg(Core core) throws IOException {
        CSVReader reader = new SimpleReader();
        reader.setQuoteCharacters(new char[]{'\"'});
        reader.setTrim(true);
        URL url = this.getClass().getClassLoader().getResource("resources/schema.org/all-classes.csv");
        InputStream in = url.openStream();
        List items = reader.parse(in);
        String[] first = (String[]) items.get(0);
        //System.out.println(Arrays.asList(first));
        for (int i = 1; i < items.size(); i++) {
            String[] line = (String[]) items.get(i);
            //System.out.println("  " + Arrays.asList(line));
            String id = line[0];
            String label = line[1];
            String comment = line[2];
            //List<String> ancestors = Arrays.asList(line[3].split(" "));
            List<String> supertypes = Arrays.asList(line[4].split(" "));
            //List<String> subtypes = Arrays.asList(line[5].split(" "));
            //List<String> properties;
            /*if ((line.length >= 7) && (line[6].length() > 0))
            properties = Arrays.asList(line[6].split(" "));
            else
            properties = Collections.EMPTY_LIST;*/
            //System.out.println(id + " " + label);
            //System.out.println("  " + supertypes);
            //System.out.println("  " + properties);
            NTag t = new NTag(id, label, supertypes);
            t.description = comment;
            core.save(t);
        }
        in.close();
        URL url2 = this.getClass().getClassLoader().getResource("resources/schema.org/all-properties.csv");
        in = url2.openStream();
        items = reader.parse(in);
        first = (String[]) items.get(0);
        //System.out.println(Arrays.asList(first));
        for (int i = 1; i < items.size(); i++) {
            String[] line = (String[]) items.get(i);
            //System.out.println("  " + Arrays.asList(line));
            //[id, label, comment, domains, ranges]
            String id = line[0];
            String label = "";
            String comment = "";
            if (line.length > 1) {
                label = line[1];
            }
            if (line.length > 2) {
                comment = line[2];
            }
            List<String> domains;
            List<String> ranges;
            if ((line.length >= 4) && (line[3].length() > 0)) {
                domains = Arrays.asList(line[3].split(" "));
            } else {
                domains = Collections.EMPTY_LIST;
            }
            if ((line.length >= 5) && (line[4].length() > 0)) {
                ranges = Arrays.asList(line[4].split(" "));
            } else {
                ranges = Collections.EMPTY_LIST;
            }
            NProperty p = new NProperty(id, label, domains, ranges);
            p.description = comment;
            core.save(p);
        }
        in.close();
    }
    
}
