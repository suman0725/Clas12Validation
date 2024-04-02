package modules;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.physics.Particle;
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
public class CVTTRACKSModule extends Module {

    public CVTTRACKSModule() {
        super("CVTTRACK");
    }

    @Override
    public void createHistos() {
        H1F hchi2 = new H1F("hchi2", "hchi2", 100, 0, 10.0);
        hchi2.setTitleX("Chi2");
        hchi2.setTitleY("Counts");
        H1F hvzn = new H1F("hvzn", "hvzn", 100, -10.0, 10.0);
        hvzn.setTitleX("Vz");
        hvzn.setTitleY("Counts");
        H1F hvzp = new H1F("hvzp", "hvzp", 100, -10.0, 10.0);
        hvzp.setTitleX("Vz");
        hvzp.setTitleY("Counts");
        DataGroup dcvtrk = new DataGroup(2, 1);
        dcvtrk.addDataSet(hvzn, 0);
        dcvtrk.addDataSet(hvzp, 1);
        this.setHistos(dcvtrk);
    }


    @Override
    public void fillHistos(Event event) {
        for (int i =0;i<event.getParticles().size();i++) {
            Particle r = event.getParticles().get(i);
            int charge = r.charge();
            if (charge == 1) {
                this.getHistos().getH1F("hvzp").fill(r.vz());
            }else if(charge==-1){
                this.getHistos().getH1F("hvzn").fill(r.vz());
            }
        }
    }

    @Override
    public void testHistos() {
        double meann = this.getHistos().getH1F("hvzn").getMean();
        double meanp = this.getHistos().getH1F("hvzp").getMean();
        System.out.println("\n#############################################################");
        System.out.println(String.format("mean negative = %.3f", meann));
        System.out.println(String.format("mean positive= %.3f", meanp));
        System.out.println("#############################################################");
        assertEquals(meann<0.&&meanp<0.,true);

    }

}


