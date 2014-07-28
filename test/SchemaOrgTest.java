
import jnetention.util.SchemaOrg;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jnetention.Core;
import jnetention.NProperty;
import jnetention.NTag;
import org.junit.Test;
import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author me
 */
public class SchemaOrgTest {

    
    @Test
    public void testSchemaOrg() throws IOException {
        Core c = new Core();
        
        new SchemaOrg(c);
        
    }
    
    public SchemaOrgTest() {
    }
    
}
