import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.BundleUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

public class SampleClient {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        
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

}
