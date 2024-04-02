package validation;

import java.util.List;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;


/**
 *
 * @author fizikci0147
 * @author devita
 */
public class Module {    
    
    private final String moduleName;
    private DataGroup    moduleGroup = null;
    
    private int nevents;

    public Module(String name){                               
        this.moduleName = name;
        this.init();
    }

    public void analyzeHistos() {
        // analyze the histograms at the end of the file processing
    }

    
    public void createHistos() {
        // create histograms
    }
    
    public void testHistos() {
        // run tests on the filled histograms
    }
    
    public void fillHistos(Event event) {
        // fill the histograms
    }

    public final String getName() {
        return moduleName;
    }

    public final int getNevents() {
        return nevents;
    }
    
    public DataGroup getHistos() {
        return moduleGroup;
    }
    
    public final void init() {
        this.nevents = 0;
        createHistos();
    }
    
    public final void processEvent(Event event) {
        // process event
        this.nevents++;
        this.fillHistos(event);
    }

    
    public void plotHistos(EmbeddedCanvas canvas) {

    }
    

    public final void setHistos(DataGroup group) {
        this.moduleGroup = group;
    }

    public final void readDataGroup(TDirectory dir) {
        String folder = this.getName() + "/";
        System.out.println("Reading from: " + folder);
        DataGroup sum = this.getHistos();
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        DataGroup newSum = new DataGroup(ncols,nrows);
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                System.out.println("\t --> " + ds.getName());
                newSum.addDataSet(dir.getObject(folder, ds.getName()),i);
            }
        }            
        this.setHistos(newSum);
    }
    
    public final void writeDataGroup(TDirectory dir) {
        String folder = "/" + this.getName();
        dir.mkdir(folder);
        dir.cd(folder);
        DataGroup sum = this.getHistos();
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                System.out.println("\t --> " + ds.getName());
                dir.addDataSet(ds);
            }
        }            
    }
//gaussian fit
    public F1D fitGauss(H1F histo,double min, double max) {
        double tmp_Mean = histo.getMean();
        int Max_Bin = histo.getMaximumBin();
        double tmp_Amp = histo.getBinContent(Max_Bin);
        double tmp_sigma = histo.getRMS();
        //System.out.println(tmp_Amp);
        F1D f1 = new F1D("f1", "[amp]*gaus(x,[mean],[sigma])",min,max);
        f1.setParameter(0, tmp_Amp);
        f1.setParameter(1, tmp_Mean);
        f1.setParameter(2, tmp_sigma / 2);
        f1.setLineColor(5);
        f1.setLineWidth(7);
        f1.setOptStat(111110);
        DataFitter.fit(f1, histo, "Q");
        return f1;
    }
    
} 
