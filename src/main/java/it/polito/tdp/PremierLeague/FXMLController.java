package it.polito.tdp.PremierLeague;

import java.net.URL;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.PremierLeague.model.Match;
import it.polito.tdp.PremierLeague.model.MatchesPair;
import it.polito.tdp.PremierLeague.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController 
{
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCreaGrafo;

    @FXML
    private Button btnConnessioneMassima;

    @FXML
    private Button btnCollegamento;

    @FXML
    private TextField txtMinuti;

    @FXML
    private ComboBox<Match> cmbM1;

    @FXML
    private ComboBox<Match> cmbM2;

    @FXML
    private ComboBox<Month> cmbMese;

    @FXML
    private TextArea txtResult;
    
    private Model model;
    
    
    @FXML
    void doCreaGrafo(ActionEvent event) 
    {
    	Month selectedMonth = this.cmbMese.getValue();
    	
    	if(selectedMonth == null)
    	{
    		this.txtResult.setText("Errore: selezionare un mese dal menù a tendina");
    		return;
    	}
    	
    	String minMinutesInput = this.txtMinuti.getText();
    	
    	if(minMinutesInput == null || minMinutesInput.isBlank())
    	{
    		this.txtResult.setText("Errore: inserire un valore (MIN) di minuti minimi");
    		return;
    	}
    	
    	minMinutesInput = minMinutesInput.trim();
    	
    	int minMinutes;
    	try
		{
			minMinutes = Integer.parseInt(minMinutesInput);
		}
		catch(NumberFormatException nfe)
		{
			this.txtResult.setText("Errore: inserire un valore (MIN) intero valido di minuti minimi");
    		return;
		}
    	
    	if(minMinutes < 0)
    	{
    		this.txtResult.setText("Errore: inserire un valore (MIN) intero non negativo di minuti minimi");
    		return;
    	}
    	
    	this.model.createGraph(minMinutes, selectedMonth);
    	
    	//print graph info
    	int numVertices = this.model.getNumVertices();
    	int numEdges = this.model.getNumEdges();
    	
    	String output = this.printGraphInfo(numVertices, numEdges);
    	this.txtResult.setText(output);
    	
    	//update UI
    	List<Match> orderedMatches = this.model.getOrderedMatches();
    	
    	this.cmbM1.getItems().clear();
    	this.cmbM1.getItems().addAll(orderedMatches);
    	
    	this.cmbM2.getItems().clear();
    	this.cmbM2.getItems().addAll(orderedMatches);
    }
    
    private String printGraphInfo(int numVertices, int numEdges)
	{
		if(numVertices == 0)
			return "Il grafo creato è vuoto (#Vertici: 0)";
		
		return String.format("Grafo creato:\n#Vertici: %d\n#Archi: %d", numVertices, numEdges);
	}

	@FXML
    void doConnessioneMassima(ActionEvent event) 
    {
		if(!this.model.isGraphCreated())
		{
			this.txtResult.setText("Errore: creare prima il grafo");
    		return;
		}
		
		Collection<MatchesPair> maxPlayersPairs = this.model.getMaxPlayersMatchesPairs();
		
		String output = this.printMaxPlayersPairs(maxPlayersPairs);
		this.txtResult.setText(output);
    }
    
    private String printMaxPlayersPairs(Collection<MatchesPair> maxPlayersPairs)
	{
		if(maxPlayersPairs.isEmpty())
			return "Nessuna coppia di match trovata con connessioni massime";
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Numero di coppie con connessioni massime: ").append(maxPlayersPairs.size());
		sb.append("\n");
		
		for(MatchesPair pair : maxPlayersPairs)
		{
			sb.append("\n").append(pair);
			sb.append("\n");
		}
    	
		return sb.toString();
	}

	@FXML
    void doCollegamento(ActionEvent event) 
    {
		if(!this.model.isGraphCreated())
		{
			this.txtResult.setText("Errore: creare prima il grafo");
    		return;
		}
		
		Match selectedMatch1 = this.cmbM1.getValue();
		Match selectedMatch2 = this.cmbM2.getValue();
		
		if(selectedMatch1 == null || selectedMatch2 == null)
		{
			this.txtResult.setText("Errore: selezionare entrambi i match dai 2 menù a tendina");
    		return;
		}
		
		if(selectedMatch1.equals(selectedMatch2))
		{
			this.txtResult.setText("Errore: i 2 match non possono essere uguali!");
    		return;
		}
		
		Collection<List<Match>> bestPaths = 
					this.model.getBestPathsBetween(selectedMatch1, selectedMatch2);
		
		String output = this.printBestPaths(bestPaths, selectedMatch1, selectedMatch2);
		this.txtResult.setText(output);
    }

    private String printBestPaths(Collection<List<Match>> bestPaths, Match match1, Match match2)
	{
		if(bestPaths.isEmpty())
			return "Non esistono percorsi migliori tra i 2 match: " +
				match1.toString() + " e " + match2.toString();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Percorsi migliori tra ").append(match1.toString()).append(" e ").append(match2.toString()).append(":\n");
		
		int count = 0;
		for(List<Match> bestPath : bestPaths)
		{
			count++;
			
			sb.append("\n").append("-".repeat(7)).append(" ").append(count).append(" ").append("-".repeat(7));
			
			int cnt = 0;
			for(Match m : bestPath)
			{
				cnt++;
				
				sb.append("\n").append(cnt).append(") ").append(m);
			}
			
			sb.append("\n");
		}
		
		return sb.toString();
	}

	@FXML
    void initialize() 
    {
        assert btnCreaGrafo != null : "fx:id=\"btnCreaGrafo\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnConnessioneMassima != null : "fx:id=\"btnConnessioneMassima\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnCollegamento != null : "fx:id=\"btnCollegamento\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtMinuti != null : "fx:id=\"txtMinuti\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbM1 != null : "fx:id=\"cmbM1\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbM2 != null : "fx:id=\"cmbM2\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbMese != null : "fx:id=\"cmbMese\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Scene.fxml'.";
    }
    
    public void setModel(Model model)
    {
    	this.model = model;
    	
    	List<Month> allMonths = this.model.getAllMonths();
    	this.cmbMese.getItems().addAll(allMonths);
    }
}
