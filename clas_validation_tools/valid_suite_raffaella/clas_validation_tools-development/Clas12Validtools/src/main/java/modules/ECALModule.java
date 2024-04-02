package modules;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.CalorimeterResponse;
import validation.Event;
import validation.Module;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author fizikci0147
 * @author devita
 */
public class ECALModule extends Module {

    public ECALModule() {
        super("ECAL");
    }

    @Override
    public void createHistos() {
        H1F hi_sf = new H1F("Sampling Fraction", "Sampling Fraction", 100, 0., 0.5);
        hi_sf.setTitleX("E/P");
        hi_sf.setTitleY("Counts");
        DataGroup dg = new DataGroup(1, 1);
        dg.addDataSet(hi_sf, 0);
        this.setHistos(dg);
    }
    @Override
    public void analyzeHistos() {
        this.fitGauss(this.getHistos().getH1F("Sampling Fraction"),0.,0.5);
    }

    @Override
    public void fillHistos(Event event) {
        if (event.getParticles().size() > 0) {
            int pid      = event.getParticles().get(0).pid();
            int status   = (int) event.getParticles().get(0).getProperty("status");
            int detector = (int) Math.abs(status)/1000;
            if(pid==11 && detector == 2) {
                double p =  event.getParticles().get(0).p();
                double E = 0;
               for(DetectorResponse r : event.getECALMap().get(0)) {
                    CalorimeterResponse response = (CalorimeterResponse) r;
                    E += response.getEnergy();
                }
                this.getHistos().getH1F("Sampling Fraction").fill(E/p);
            }
        }
    }

    @Override
    public void testHistos() {
        double mean = this.getHistos().getH1F("Sampling Fraction").getMean();
        System.out.println("\n#############################################################");
        System.out.println(String.format("mean = %.3f", mean));;
        System.out.println("#############################################################");
        assertEquals(mean>0.2&&mean<0.3,true);

    }

}
