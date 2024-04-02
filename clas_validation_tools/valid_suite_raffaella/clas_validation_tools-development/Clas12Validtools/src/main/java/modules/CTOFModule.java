package modules;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.ScintillatorResponse;
import validation.Event;
import validation.Module;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.clas.pdg.PhysicsConstants;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author fizikci0147
 * @author devita
 */
public class CTOFModule extends Module {

    public CTOFModule() { super("CTOF"); }

    @Override
    public void createHistos() {
        H1F hsc_energy = new H1F("hsc_energy", "hsc_energy", 100, 0.0, 300.0);
        hsc_energy.setTitleX("Energy");
        hsc_energy.setTitleY("Counts");
        H1F hsvt = new H1F("hsvt", "hsvt", 1000, -5.0, 5.0);
        hsvt.setTitleX("Electron Vertex Time");
        hsvt.setTitleY("Counts");
        DataGroup dscinth = new DataGroup(1, 1);
        dscinth.addDataSet(hsvt, 0);
        this.setHistos(dscinth);

    }

    @Override
    public void fillHistos(Event event) {

        if (event.getParticles().size() > 0 &&
                event.getParticles().get(0).pid()==11 &&
                (int) Math.abs(event.getParticles().get(0).getProperty("status"))/1000 == 2 &&
                !event.getCTOFMap().isEmpty()) {
            for(int i=0; i<event.getParticles().size(); i++) {
                int pid = event.getParticles().get(i).pid();
                int status = (int) event.getParticles().get(i).getProperty("status");
                int detector = (int) Math.abs(status) / 1000;
                double vt =event.getParticles().get(i).getProperty("vt");
                if (pid == -211 && detector == 4) {
                    if(event.getCTOFMap().containsKey(i)) {
                        for (DetectorResponse r : event.getCTOFMap().get(i)) {
                            ScintillatorResponse response = (ScintillatorResponse) r;
                            int layer = response.getDescriptor().getLayer();
                            double vertt = response.getTime() - response.getPath()/PhysicsConstants.speedOfLight() - vt;
                            this.getHistos().getH1F("hsvt").fill(vertt);

                        }
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
        System.out.println(String.format("CTOF Vt Mean = %.3f", mean));
        System.out.println(String.format("CTOF Vt sigma = %.3f", sigma));
        System.out.println("#############################################################");
        assertEquals(mean>0.0001,true);

    }
    @Override
    public void analyzeHistos() {this.fitGauss(this.getHistos().getH1F("hsvt"),-5, 5);}
}
