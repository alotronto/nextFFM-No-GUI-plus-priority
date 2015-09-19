import SimulatedAnnealingUtility.SimAnne;

public class StartAppl {

	public static void main(String[] args) {

		// Controllo il numero di argomenti
		if (args.length != 9) {
			System.out.println("numero o tipo parapetri passati non valido");
		} else {
			DataBaseUtility.Utility.setHost(String.valueOf(args[0]));
			DataBaseUtility.Utility.setDB(String.valueOf(args[1]));
			DataBaseUtility.Utility.setUser(String.valueOf(args[2]));
			DataBaseUtility.Utility.setPass(String.valueOf(args[3]));

			DataStructure.Utility.initCostoCarguranti();
			//DataStructure.Utility.initMapSpostamenti();
			DataStructure.Utility.initMapSpostamentiNewVersion();
			DataStructure.Utility.initSquadre();
			// DataStructure.Utility.initInterventi();
			// DataStructure.Utility.initInterventiRandom(Integer.valueOf(args[8]),
			// Integer.valueOf(args[9]), Integer.valueOf(args[10]));

			// Manage the 8th parameter of Main
			String listOfIDString = args[8];
			String[] ArrayoflistOfID = listOfIDString.split("-");
			int[] listOfID = new int[ArrayoflistOfID.length];
			for (int i = 0; i < ArrayoflistOfID.length; i++)
				listOfID[i] = Integer.valueOf(ArrayoflistOfID[i]);
			// End of Manage 8th parameter of Main

			DataStructure.Utility.initInterventiFromList(listOfID);

			// DataStructure.Utility.createInitialSolutionUniform();
			DataStructure.Utility.createInitialSolution();

			// DEBUG
			// Controllo della soluzione iniziale
			System.out.println(DataStructure.Utility.initialSolution.toString());

			SimAnne sa = new SimAnne(Integer.valueOf(args[4]), Integer.valueOf(args[5]), Float.valueOf(args[6]),
					Float.valueOf(args[7]));
			for (int z = 0; z < 10; z++) {
				// Debug Message
				System.out.println("Test: " + z);

				sa.findBestSolution();
				// System.out.println(DataStructure.Utility.initialSolution.toString());
				// ############# Salvataggio del risultato su file

				DataStructure.Utility.openFile("result_" + sa.getnIteration() + "_" + sa.getnValuate() + "_"
						+ sa.getCooling() + "_" + sa.getAlfa() + ".txt");
				DataStructure.Utility.printToFile(DataStructure.Utility.initialSolution.getCosto() + "\n");
				DataStructure.Utility.closefile();

				DataStructure.Utility.openFile("result_full" + sa.getnIteration() + "_" + sa.getnValuate() + "_"
						+ sa.getCooling() + "_" + sa.getAlfa() + ".txt");
				DataStructure.Utility.printToFile(DataStructure.Utility.initialSolution.toString() + "\n");
				DataStructure.Utility.closefile();

				// DataStructure.Utility.createInitialSolutionUniform();
				DataStructure.Utility.createInitialSolution();
			}
			  
			 

		}
	}
}
// ####################################################

// DataStructure.Utility.distribuzionePercentuale(sa.getnIteration()+ "_" +
// sa.getnValuate() + "_" + sa.getCooling() + "_"+ sa.getAlfa());

// DataStructure.Utility.createMediaIterTime(sa.getnIteration(),sa.getnValuate(),
// sa.getCooling(), sa.getAlfa());

