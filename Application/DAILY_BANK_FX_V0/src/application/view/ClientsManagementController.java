package application.view;

/**
 * Fenetre de gestion des clients (première page, liste des clients)
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.ClientsManagement;
import application.control.ExceptionDialog;
import application.tools.AlertUtilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.orm.AccessPrelevement;
import model.orm.exception.ApplicationException;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;

public class ClientsManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private ClientsManagement cm;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Client> olc;

	// Manipulation de la fenêtre
	/**
	 * Définit les variables de la fenetre
	 * 
	 * @param _primaryStage : scene
	 * @param _cm           : fenetre
	 * @param _dbstate      : données de la session de l'utilisateur
	 */
	public void initContext(Stage _primaryStage, ClientsManagement _cm, DailyBankState _dbstate) {
		this.cm = _cm;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olc = FXCollections.observableArrayList();
		this.lvClients.setItems(this.olc);
		this.lvClients.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvClients.getFocusModel().focus(-1);
		this.lvClients.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
		this.validateComponentState();
	}

	/**
	 * Affiche la fenetre et attend une action
	 */
	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private TextField txtNum;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private ListView<Client> lvClients;
	@FXML
	private Button btnDesactClient;
	@FXML
	private Button btnModifClient;
	@FXML
	private Button btnComptesClient;
	@FXML
	private Button btnGenererReleves;
	@FXML
	private Button btnExecPrelevements;
	
	/**
	 * Redéfinition de la fonction initialize
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}
	
	@FXML
	private void doGenererReleves() {
		this.cm.genererReleves();
	}
	
	@FXML
	private void doExecPrelevements() {
		String result=null;
		boolean continuer = AlertUtilities.confirmYesCancel(primaryStage, "Exécuter les prélèvements", "Exécuter les prélèvements automatiques aujourd'hui ", "êtes-vous sûr de vouloir exécuter tous les prélèvements \nautomatiques de ce jour ?", AlertType.CONFIRMATION);
		if(continuer) {
			try {
				AccessPrelevement ap=new AccessPrelevement();
				result=ap.executePrelevement();
			} catch (DatabaseConnexionException exc) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, exc);
				ed.doExceptionDialog();
				this.primaryStage.close();
			} catch (DataAccessException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
			}
			if (result!=null) {
				result="   "+result.replaceAll(" - ", "\n");
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Erreur d'exécution des prélèvements automatiques");
				alert.setHeaderText("Certains compte ne sont pas assez approvisionnés pour éxécuter les prélèvements");
				alert.setContentText(result);
				System.out.println(result);
				alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
				alert.show();
			}
		}
		
	}

	@FXML
	private void doRechercher() {
		int numCompte;
		try {
			String nc = this.txtNum.getText();
			if (nc.equals("")) {
				numCompte = -1;
			} else {
				numCompte = Integer.parseInt(nc);
				if (numCompte < 0) {
					this.txtNum.setText("");
					numCompte = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtNum.setText("");
			numCompte = -1;
		}

		String debutNom = this.txtNom.getText();
		String debutPrenom = this.txtPrenom.getText();

		if (numCompte != -1) {
			this.txtNom.setText("");
			this.txtPrenom.setText("");
		} else {
			if (debutNom.equals("") && !debutPrenom.equals("")) {
				this.txtPrenom.setText("");
			}
		}

		// Recherche des clients en BD. cf. AccessClient > getClients(.)
		// numCompte != -1 => recherche sur numCompte
		// numCompte != -1 et debutNom non vide => recherche nom/prenom
		// numCompte != -1 et debutNom vide => recherche tous les clients
		ArrayList<Client> listeCli;
		listeCli = this.cm.getlisteComptes(numCompte, debutNom, debutPrenom);

		this.olc.clear();
		for (Client cli : listeCli) {
			this.olc.add(cli);
		}

		this.validateComponentState();
	}

	@FXML
	private void doComptesClient() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client client = this.olc.get(selectedIndice);
			this.cm.gererComptesClient(client);
		}
	}

	@FXML
	private void doModifierClient() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client cliMod = this.olc.get(selectedIndice);
			Client result = this.cm.modifierClient(cliMod);
			if (result != null) {
				this.olc.set(selectedIndice, result);
			}
		}
	}

	@FXML
	private void doDesactiverClient() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client cliMod = this.olc.get(selectedIndice);
			Client result = this.cm.desactiverClient(cliMod);
			if (result != null) {
				this.olc.set(selectedIndice, result);
				this.validateComponentState();
			}
		}
	}

	@FXML
	private void doNouveauClient() {
		Client client;
		client = this.cm.nouveauClient();
		if (client != null) {
			this.olc.add(client);
		}
	}

	private void validateComponentState() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		boolean estChefDagence = this.dbs.isChefDAgence();
		
		if (selectedIndice >= 0) {
			Client cli = this.olc.get(selectedIndice);
			boolean inactif = cli.estInactif.equals("O");
			
			this.btnModifClient.setDisable(false);
			this.btnComptesClient.setDisable(inactif);
			this.btnDesactClient.setDisable(inactif || !estChefDagence);
		} else {
			this.btnModifClient.setDisable(true);
			this.btnComptesClient.setDisable(true);
			this.btnDesactClient.setDisable(true);
		}
		this.btnExecPrelevements.setDisable(!estChefDagence);
	}
}
