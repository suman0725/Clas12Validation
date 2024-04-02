package validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.clas.detector.*;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author fizikci0147
 * @author devita
 */
public class Event {
    private boolean debug = false;
    
    private DataBank runBank = null;
    private DataBank mcBank = null;
    private DataBank recBank = null; 
    private DataBank recPartBank = null; 
    private DataBank recFtPartBank = null; 
    private DataBank recTrkBank = null;
    private DataBank recFtBank = null;
    private DataBank recCalBank = null;
    private DataBank recSciBank = null;
    private DataBank recCheBank = null;

    private List<Particle> particles = new ArrayList<>();
    private List<Particle> mcParticles = new ArrayList<>();
    private Map<Integer, List<DetectorResponse>>  ecalMap = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  htccMap = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  ltccMap = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  ftofMap = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  ctofMap = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  cndMap  = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  dcMap   = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  cvtMap  = new HashMap<>();
    private Map<Integer, List<DetectorResponse>>  ftMap   = new HashMap<>(); //ftc
    private Map<Integer, List<DetectorResponse>>  fthMap   = new HashMap<>();


    public Event(DataEvent event) {
        this.readEvent(event);
        if(debug) System.out.println("Read event with " + particles.size() + " particles");
    }
    


    private DataBank getBank(DataEvent de, String bankName) {
        DataBank bank = null;
        if (de.hasBank(bankName)) {
            bank = de.getBank(bankName);
        }
        return bank;
    }

    private void getBanks(DataEvent de) {
        runBank       = getBank(de, "RUN::config");
        mcBank        = getBank(de, "MC::Particle");
        recBank       = getBank(de, "REC::Event");
        recPartBank   = getBank(de, "REC::Particle");
        recFtPartBank = getBank(de, "RECFT::Particle");
        recCheBank    = getBank(de, "REC::Cherenkov");
        recCalBank    = getBank(de, "REC::Calorimeter");
        recSciBank    = getBank(de, "REC::Scintillator");
        recTrkBank    = getBank(de, "REC::Track");
        recFtBank     = getBank(de, "REC::ForwardTagger");
    }

    private void readParticles() {
        if(recPartBank!=null) {
            int rows = recPartBank.rows();
            for (int loop = 0; loop < rows; loop++) {
                int pid    = recPartBank.getInt("pid", loop);
                int charge = recPartBank.getByte("charge", loop);
                if(pid==0) {
                    if (recPartBank.getByte("charge", loop) == -1)     pid = -211;
                    else if (recPartBank.getByte("charge", loop) == 1) pid = 211;
                    else pid = 22;
                }
                Particle recParticle = new Particle(
                        pid,
                        recPartBank.getFloat("px", loop),
                        recPartBank.getFloat("py", loop),
                        recPartBank.getFloat("pz", loop),
                        recPartBank.getFloat("vx", loop),
                        recPartBank.getFloat("vy", loop),
                        recPartBank.getFloat("vz", loop));
                double vt  = recPartBank.getFloat("vt", loop);
                int status = recPartBank.getInt("status", loop);
                recParticle.setProperty("vt", vt);
                recParticle.setProperty("status", status);
                particles.add(recParticle);
            }
        }
    }
            
    private void readScintillatorResponses() {
        if(recSciBank!=null) {
            ftofMap.clear();
            ctofMap.clear();
            cndMap.clear();
            int rows = recSciBank.rows();
            for (int loop = 0; loop < rows; loop++) {
                int index     = recSciBank.getInt("index", loop);
                int pindex    = recSciBank.getInt("pindex", loop);
                int detector  = recSciBank.getByte("detector", loop);
                int layer     = recSciBank.getByte("layer", loop);
                int sector    = recSciBank.getByte("sector", loop);
                int paddle    = recSciBank.getInt("component", loop);
                ScintillatorResponse response = new ScintillatorResponse(layer, sector, paddle);
                double energy = recSciBank.getFloat("energy", loop);
                double time   = recSciBank.getFloat("time", loop);
                double x      = recSciBank.getFloat("x", loop);
                double y      = recSciBank.getFloat("y", loop);
                double z      = recSciBank.getFloat("z", loop);
                double hx     = recSciBank.getFloat("hx", loop);
                double hy     = recSciBank.getFloat("hy", loop);
                double hz     = recSciBank.getFloat("hz", loop);
                double path   = recSciBank.getFloat("path", loop);
                response.setHitIndex(index);
                response.setPosition(x,y,z);
                response.setEnergy(energy);
                response.setTime(time);
                response.setMatchPosition(hx, hy, hz);
                response.setPath(path);
                response.setAssociation(pindex);

                // FIXME add dE/dx and cluster size
                if(detector==DetectorType.FTOF.getDetectorId()) {
                    this.loadMap(ftofMap, response);
                }
                else if(detector==DetectorType.CTOF.getDetectorId()) {
                    this.loadMap(ctofMap, response);
                }
                else if(detector==DetectorType.CND.getDetectorId())  {
                    this.loadMap(cndMap, response);
                }
            }           
        }    
    }

    //Calorimeter
    private void readCalorimeterResponses() {
        if (recCalBank!=null) {
            ecalMap.clear();
            int rows = recCalBank.rows();
            for (int loop = 0; loop < rows; loop++) {
                int index     = recCalBank.getInt("index", loop);
                int pindex    = recCalBank.getInt("pindex", loop);
                int sector    = recCalBank.getByte("sector", loop);
                int layer     = recCalBank.getByte("layer", loop);
                int detector  = recCalBank.getByte("detector", loop);
                double x      = recCalBank.getFloat("x", loop);
                double y      = recCalBank.getFloat("y", loop);
                double z      = recCalBank.getFloat("z", loop);
                double hx     = recCalBank.getFloat("hx", loop);
                double hy     = recCalBank.getFloat("hy", loop);
                double hz     = recCalBank.getFloat("hz", loop);
                double path   = recCalBank.getFloat("path", loop);
                double energy = recCalBank.getFloat("energy", loop);
                double time   = recCalBank.getFloat("time", loop);
                int status    = recCalBank.getShort("status", loop);
                CalorimeterResponse response = new CalorimeterResponse(sector, layer, 0);

                response.setHitIndex(index);
                response.setPosition(x, y, z);
                response.setEnergy(energy);
                response.setTime(time);
                response.setMatchPosition(hx, hy, hz);
                response.setPath(path);
                response.setAssociation(pindex);
                if(detector == DetectorType.ECAL.getDetectorId()) {
                    this.loadMap(ecalMap, response);
                }
            }
        }
    }
    //Cherenkov
    private void readCherenkovResponses() {
        if(recCheBank!=null) {
            htccMap.clear();
            ltccMap.clear();
            int rows = recCheBank.rows();
            //  System.out.println("rows Cherenkov: ");
            //  System.out.println(rows);
            for (int loop = 0; loop < rows; loop++) {
                int index     = recCheBank.getInt("index", loop);
                int pindex    = recCheBank.getInt("pindex", loop);
                int detector  = recCheBank.getByte("detector", loop);
                float x       = recCheBank.getFloat("x", loop);
                float y       = recCheBank.getFloat("y", loop);
                float z       = recCheBank.getFloat("z", loop);
                double time   = recCheBank.getFloat("time", loop);
                double nphe   = recCheBank.getFloat("nphe", loop);
                double dtheta = recCheBank.getFloat("dtheta", loop);
                double dphi   = recCheBank.getFloat("dphi", loop);

                CherenkovResponse response = new CherenkovResponse(dtheta,dphi);
                response.setHitIndex(index);
                response.setAssociation(pindex);
                response.setEnergy(nphe);
                response.setTime(time);
                response.setPosition(x, y, z);
                if(detector == DetectorType.HTCC.getDetectorId()) {
                    this.loadMap(htccMap, response);
                } else if(detector == DetectorType.LTCC.getDetectorId()) {
                    this.loadMap(ltccMap, response);
                }
            }
        }
    }
//// FORWARD TAGGER
    private void readTaggerResponses() {
        if (recFtBank != null) {
            ftMap.clear();
            int rows = recFtBank.rows();
            for (int loop = 0; loop < rows; loop++) {
              //  int id  = recFtBank.getShort("id", loop);
             //   int size = recFtBank.getShort("size", loop);
                int index     = recFtBank.getInt("index", loop);
                int pindex    = recFtBank.getInt("pindex", loop);
                int detector  = recFtBank.getByte("detector", loop);
                double x      = recFtBank.getFloat("x", loop);
                double y      = recFtBank.getFloat("y", loop);
                double z      = recFtBank.getFloat("z", loop);
                double dx     = recFtBank.getFloat("dx", loop);
                double dy     = recFtBank.getFloat("dy", loop);
                double radius = recFtBank.getFloat("radius", loop);
                double time   = recFtBank.getFloat("time", loop);
                double energy = recFtBank.getFloat("energy", loop);

                double z0 = 0; // FIXME vertex
                double path = Math.sqrt(x * x + y * y + (z - z0) * (z - z0));
                double cx = x / path;
                double cy = y / path;
                double cz = (z - z0) / path;

                TaggerResponse response = new TaggerResponse();
                response.setHitIndex(index);
                response.setAssociation(pindex);
                response.setPosition(x, y, z);
                response.setHitIndex(loop);
                response.setEnergy(energy);
                response.setRadius(radius);
                response.setTime(time);
                response.setMomentum(energy * cx, energy * cy, energy * cz);

                if(detector == DetectorType.FTCAL.getDetectorId()) { //FT or FTCAL ??j
                    this.loadMap(ftMap, response);
                }else if(detector == DetectorType.FTHODO.getDetectorId()) {
                    this.loadMap(fthMap, response);

                }
            }
        }
    }

    private void readTrackResponses(){
        if (recTrkBank!= null){
            dcMap.clear();
            cvtMap.clear();
        int rows = recTrkBank.rows();
        for (int loop = 0; loop < rows; loop++) {
            int index = recTrkBank.getInt("index", loop);
            int pindex = recTrkBank.getInt("pindex", loop);
            double chi2 = recTrkBank.getFloat("chi2", loop);
            int NDF = recTrkBank.getInt("NDF", loop);
            int detector = recTrkBank.getByte("detector", loop);
            int sector = recTrkBank.getByte("sector", loop);
            //DetectorTrack response =new DetectorTrack();

            if (detector == DetectorType.DC.getDetectorId()) {
                //this.loadMap(dcMap, response);
            } else if (detector == DetectorType.CVT.getDetectorId()) {
                //this.loadMap(cvtMap, response);
            }

        }
        }
    }
        
    private void loadMap(Map<Integer, List<DetectorResponse>> map, DetectorResponse response) {
        final int iTo = response.getAssociation();
        if (map.containsKey(iTo)) {
            map.get(iTo).add(response);
        } else {
            List<DetectorResponse> iFrom = new ArrayList<>();
            map.put(iTo, iFrom);
            map.get(iTo).add(response);
        }
    }

    private void readEvent(DataEvent de) {
        this.getBanks(de);
        this.readParticles();
        this.readScintillatorResponses();
        this.readCherenkovResponses();
        this.readCalorimeterResponses();
        this.readTaggerResponses();
        this.readTrackResponses();

    }

    public List<Particle> getParticles() {
        return particles;
    }
 
    public Map<Integer, List<DetectorResponse>> getFTOFMap() {
        return ftofMap;
    }

    public Map<Integer, List<DetectorResponse>> getCTOFMap() {
        return ctofMap;
    }

    public Map<Integer, List<DetectorResponse>> getCNDMap() {
        return cndMap;
    }

    public Map<Integer, List<DetectorResponse>> getECALMap() {
        return ecalMap;
    }
    public Map<Integer, List<DetectorResponse>> getHTCCMap() {
        return htccMap;
    }
    public Map<Integer, List<DetectorResponse>> getLTCCMap() {
        return ltccMap;
    }
    public Map<Integer, List<DetectorResponse>> getDCTrkMap() {
        return dcMap;
    }
    public Map<Integer, List<DetectorResponse>> getCVTTrkMap() {
        return cvtMap;
    }
    public Map<Integer, List<DetectorResponse>> getFTMap() {
        return ftMap;
    }

    
    
}
