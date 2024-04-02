package modules;

import org.jlab.clas.detector.CherenkovResponse;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.ScintillatorResponse;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.pdg.PhysicsConstants;
import validation.Event;
import validation.Module;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import static org.junit.Assert.*;

/**
 *
 * @author fizikci0147
 * @author devita
 */
public class FTOFModule extends Module {

    public FTOFModule() {
        super("FTOF");
    }
    
    @Override
    public void createHistos() {
        H1F hsc_energy = new H1F("hsc_energy", "hsc_energy", 100, 0.0, 100.0);
        hsc_energy.setTitleX("Energy");
        hsc_energy.setTitleY("Counts");
        H1F hsvt = new H1F("hsvt", "hsvt", 100, -5.0, 5.0);
        hsvt.setTitleX("Electron Vertex Time");
        hsvt.setTitleY("Counts");
        DataGroup dscinth = new DataGroup(2, 2);
        dscinth.addDataSet(hsvt, 0);
        dscinth.addDataSet(hsc_energy, 1);
        dscinth.addDataSet(hsc_energy, 2);
        this.setHistos(dscinth);
    }
    
    @Override
    public void fillHistos(Event event) {

        if (event.getParticles().size() > 0 &&event.getFTOFMap().get(0)!=null) {
            int pid = event.getParticles().get(0).pid();
            int status = (int) event.getParticles().get(0).getProperty("status");
            int detector = (int) Math.abs(status) / 1000;

            double vt =event.getParticles().get(0).getProperty("vt");
            if (pid == 11 && detector == 2) {
                for (DetectorResponse r : event.getFTOFMap().get(0)) {
                    ScintillatorResponse response = (ScintillatorResponse) r;
                    int layer = response.getDescriptor().getLayer();
                    if (layer == 2) {
                        double vertt = response.getTime() - response.getPath()/PhysicsConstants.speedOfLight() - vt;
                        double energy = response.getEnergy();
                        this.getHistos().getH1F("hsvt").fill(vertt);
                        this.getHistos().getH1F("hsc_energy").fill(energy);


                    }
                }
            }
        }
    }


    @Override
    public void testHistos() {
        double mean = this.getHistos().getH1F("hsvt").getMean();
        double sigma = this.getHistos().getH1F("hsvt").getFunction().getParameter(2);

        System.out.println("\n#############################################################");
        System.out.println(String.format("Vt Mean = %.3f", mean));
        System.out.println(String.format("Vt Sigma = %.3f", sigma));
        System.out.println("#############################################################");
          assertEquals(mean>0.0001&&sigma>0.05,true);

    }
    @Override
    public void analyzeHistos() {this.fitGauss(this.getHistos().getH1F("hsvt"),-5, 5);}

}
