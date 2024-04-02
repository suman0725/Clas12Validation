package modules;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Particle;
import validation.Event;
import validation.Module;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;


/**
 *
 * @author fizikci0147
 * @author devita
 */
public class EventBuilderModule extends Module {

    public EventBuilderModule() { super("Particle"); }

    @Override
    public void createHistos() {
        H1F h_m1 = new H1F("h_m1", "h_m1", 100, 0.0, 0.2);
        h_m1.setTitleX("Mpi0");
        h_m1.setTitleY("Counts");

        H1F h_m2 = new H1F("h_m2", "h_m2", 100, 0.0, 0.2);
        h_m2.setTitleX("Mpi0");
        h_m2.setTitleY("Counts");

       DataGroup devent = new DataGroup(2, 1);
       devent.addDataSet(h_m1, 0);
        devent.addDataSet(h_m2, 1);
        this.setHistos(devent);

    }

    @Override
    public void fillHistos(Event event) {

        ArrayList<Particle> Ecalphoton = new ArrayList<>();
        ArrayList<Particle> FTcalphoton = new ArrayList<>();

        double mass1=0., mass2=0.;
        for (int i =0;i<event.getParticles().size();i++) {
            Particle r = event.getParticles().get(i);
            if (r.pid() == 22 && r.p()>0.5 && event.getECALMap().containsKey(i)) {
                    Ecalphoton.add(r);
            }
            if(Ecalphoton.size()>=2){
                LorentzVector v0 = Ecalphoton.get(0).vector();
                LorentzVector v1 = Ecalphoton.get(1).vector();
                LorentzVector vt = new LorentzVector(v0);
                vt.add(v1);
                this.getHistos().getH1F("h_m1").fill(vt.mass());
            }
            if (r.pid() == 22 && event.getFTMap().containsKey(i)) {
                FTcalphoton.add(r);
            }

            if(FTcalphoton.size()>=2) {
                LorentzVector v0 =FTcalphoton.get(0).vector();
                LorentzVector v1 = FTcalphoton.get(1).vector();
                LorentzVector vt = new LorentzVector(v0);
                vt.add(v1);
                this.getHistos().getH1F("h_m2").fill(vt.mass());
            }
        }
    }
    @Override
    public void testHistos() {
        double mean1 = this.getHistos().getH1F("h_m1").getFunction().getParameter(1);
        double mean2 = this.getHistos().getH1F("h_m2").getFunction().getParameter(1);
        double sigma1 = this.getHistos().getH1F("h_m1").getFunction().getParameter(2);
        double sigma2 = this.getHistos().getH1F("h_m2").getFunction().getParameter(2);
        System.out.println("\n#############################################################");
        System.out.println(String.format("mean1 = %.3f mean2 = %.3f", mean1,mean2));;
        System.out.println("#############################################################");
         assertEquals(mean1>0.1&&mean1<0.3,true);

    }
    @Override
    public void analyzeHistos()
    {
        this.fitGauss(this.getHistos().getH1F("h_m1"),0.12,0.16);
        this.fitGauss(this.getHistos().getH1F("h_m2"),0.12,0.16);
    }

}