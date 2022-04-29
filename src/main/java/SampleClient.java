import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.BundleUtil;

public class SampleClient {

    public static void main(String[] theArgs) throws IOException {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        SampleClient sampleClient = new SampleClient();
        sampleClient.basicTasks(client);
        
        sampleClient.intermediateTasks(client);
    }
    
    
    public void basicTasks(IGenericClient client) {
    	
    	 // Search for Patient resources
        FhirContext fhirContext = FhirContext.forR4();
        List<IBaseResource> patients = new ArrayList<>();
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();
        
        patients.addAll(BundleUtil.toListOfResources(fhirContext, response));
             System.out.println("Loaded " + patients.size() + " patients!");

             Collections.sort(patients, (p1, p2) -> {
                return ((Patient) p1).getNameFirstRep().getGivenAsSingleString().compareToIgnoreCase(((Patient) p2).getNameFirstRep().getGivenAsSingleString()); 
             });

             patients.forEach(p -> {
                System.out.print("Patient first name and last name:"+((Patient) p).getNameFirstRep().getNameAsSingleString());
                System.out.println(", birth date:"+String.format("%1$tb %1$te, %1$tY", ((Patient) p).getBirthDate()));
             });
        }
    
    public void intermediateTasks(IGenericClient client) throws IOException {
    	System.out.println("Avg Response time of loop 1:"+averageResponseTime(client, true));
    	System.out.println("Avg Response time of loop 2:"+averageResponseTime(client, true));
    	System.out.println("Avg Response time of loop 3:"+averageResponseTime(client, false));
    }
    
    public double averageResponseTime(IGenericClient client, boolean isCacheEnabled) throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader("src/main/resources/lastNameData.txt"));
    	br.readLine();
    	
        List<Long> list1 = new ArrayList<>();     
        String lastName;     
        
        while ((lastName = br.readLine()) != null) {
    		if(isCacheEnabled)
    			client.search()
                    .forResource("Patient")
                    .where(Patient.FAMILY.matches().value(lastName))
                    .returnBundle(Bundle.class)
                    .execute();
    		else
    			client.search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(lastName))
                .returnBundle(Bundle.class)
                .cacheControl(new CacheControlDirective().setNoCache(true))
                .execute();
    		
    		list1.add(Calendar.getInstance().getTimeInMillis());
    		
        }

    	OptionalDouble avgResponseTime = list1.stream().mapToLong(l -> l).average();
    	return avgResponseTime.getAsDouble();
		       
    }

}
