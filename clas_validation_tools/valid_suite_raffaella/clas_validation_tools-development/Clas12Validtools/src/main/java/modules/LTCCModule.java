package modules;

import org.jlab.clas.detector.CherenkovResponse;
import org.jlab.clas.detector.DetectorResponse;
import static org.junit.Assert.*;

import org.jlab.detector.base.DetectorType;
import validation.Event;
import validation.Module;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
/**
 *
 * @author fizikci0147
 * @author devita
 */
public class LTCCModule extends Module {

    public LTCCModule() {
        super("LTCC");
    }

    @Override
    public void createHistos() {
        H1F hcher_nphe = new H1F("hcher_nphe", "hcher_nphe", 50, 0, 50.0);
        hcher_nphe.setTitleX("nphe");
        hcher_nphe.setTitleY("Counts");
        H1F hcher_time = new H1F("hcher_time", "hcher_time", 100, 120.0, 130.0);
        hcher_time.setTitleX("Time");
        hcher_time.setTitleY("Counts");
        H1F hcher_phi = new H1F("hcher_phi", "hcher_phi", 100, -10., 10.);
        hcher_phi.setTitleX("Phi");
        hcher_phi.setTitleY("Counts");
        H1F hcher_theta = new H1F("hcher_theta", "hcher_theta", 100, -3., 3.);
        hcher_theta.setTitleX("#theta");
        hcher_theta.setTitleY("Counts");
        DataGroup dscher = new DataGroup(1, 1);
        dscher.addDataSet(hcher_nphe, 0);
        dscher.addDataSet(hcher_time,  1);
        dscher.addDataSet(hcher_phi,   2);
        dscher.addDataSet(hcher_theta, 3);
        this.setHistos(dscher);
    }

    @Override
    public void analyzeHistos() {
        this.fitGauss(this.getHistos().getH1F("hcher_nphe"), 0, 50);
    }

    @Override
    public void fillHistos(Event event) {
        if (event.getParticles().size() > 0) {
            int pid = event.getParticles().get(0).pid();
            int status = (int) event.getParticles().get(0).getProperty("status");
            int detector = (int) Math.abs(status) / 1000;
            if (pid == 11 && detector == 2 && event.getLTCCMap().get(0) != null) { //detector for LTCC
                for (DetectorResponse r : event.getLTCCMap().get(0)) {
                    CherenkovResponse response = (CherenkovResponse) r;
                    this.getHistos().getH1F("hcher_nphe").fill(response.getNphe());
                    this.getHistos().getH1F("hcher_time").fill(response.getTime());
                    this.getHistos().getH1F("hcher_phi").fill(Math.toDegrees(response.getHitPosition().toVector3D().phi()));
                    this.getHistos().getH1F("hcher_theta").fill(Math.toDegrees(response.getHitPosition().toVector3D().theta()));
                }
            }
        }
    }

    @Override
    public void testHistos() {
      double npe = this.getHistos().getH1F("hcher_nphe").getMean();
        System.out.println("\n#############################################################");
        System.out.println(String.format("npe/Events = %.3f", npe));;
        System.out.println("#############################################################");
        assertEquals(npe>0.2,true);

    }


}