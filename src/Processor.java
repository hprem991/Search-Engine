import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;


public class Processor {

	static Map<String, Integer> globalCollection = new TreeMap(); //  // uniqueToken -> noOfOccurances
	static Map<String, List<String> > tokenDocument = new TreeMap();  // uniqueToken -> documentID

	static Map<String, List<String> > offsetMap = new HashMap(); // uniqueToken -> Offsets in posting
	static Map<String, Integer> offsetFreq = new HashMap(); // Offset (in Posting) -> Term freq
	static Map<String, String> offsetToDoc = new HashMap(); // Offset (in posting) -> DocID

	static Map<String, Integer> documentTerms = new HashMap(); // Each Doc -> Token Count

	static Map<String, String> docSnippet = new HashMap(); // Doc -> Snippet
	static Map<String, String> docHeadline = new HashMap(); // Doc -> headline

	static int totalToken = 0;

	Processor(){
		String pwd = System.getProperty("user.dir");
		File dir = new File(pwd);
		for(File file: dir.listFiles())
			if(file.equals("output.txt"))
				file.delete();
	}

	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	private boolean Dictonary(File directory){
		File[] subdir = directory.listFiles();
		for(int index = 0; index < subdir.length; index++){
			if(subdir[index].isDirectory()){
				File[] file = subdir[index].listFiles();
				for(int count = 0; count < file.length; count++) {
					Tokenize ob;
					String fileName = file[count].toString();
					try {
						ob = new Tokenize(file[count]);
						ob.tokens();
						ob.Stem();
						ob.removeSingleLetters();
						List<String> allTokens = ob.getAllTokens();

						Map<String, Integer> localCollection = new HashMap<String, Integer>(); // token -> tf
						for(int i = 0; i < allTokens.size(); i++){
							if(localCollection.containsKey(allTokens.get(i))){
								localCollection.put(allTokens.get(i), localCollection.get(allTokens.get(i)) + 1);
							} else {
								localCollection.put(allTokens.get(i), 1 );	
							}
						}

						documentTerms.put(fileName, allTokens.size());

						for (Map.Entry<String, Integer> entry : localCollection.entrySet()) {
							String key = entry.getKey().trim();
							if(!key.isEmpty() && (key.length() > 1)) {
								if(tokenDocument.containsKey(key)) { 
									try {
										List<String> currentDocs = new ArrayList<String>();
										currentDocs = tokenDocument.get(key);
										currentDocs.add(file[count].toString());
										Collections.sort(currentDocs);
										tokenDocument.put(key, currentDocs);
									} catch (Exception e){
										System.out.println("Exception Occoured" +e.getMessage() );
									}
								} else {
									List<String> currentDocs = new ArrayList();
									currentDocs.add(file[count].toString());
									tokenDocument.put(key, currentDocs);
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
				}
			}
		}
		return true;
	}

	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	private void Posting(){
		int oldoffset = 0, newoffset = 0, tf = 0, df = 0;
		for(Map.Entry<String, Integer > entry : globalCollection.entrySet()){
			String token  = entry.getKey();
			List<String> documentList = new ArrayList<String>();
			documentList = tokenDocument.get(token);
			if(!token.isEmpty() && (documentList != null)){
				for(int count = 0; count < documentList.size(); count++ ) {
					String file = documentList.get(count);
					File fileName = new File(file);
					int termFreq = 0;
					try {
						Tokenize ob = new Tokenize(fileName);
						ob.tokens();
						ob.Stem();
						ob.removeSingleLetters();
						List<String> allTokens = ob.getAllTokens();

						for(int i = 0; i < allTokens.size() ;i++){
							if(allTokens.get(i).equals(token)){
								termFreq++;
							} 
						}

					} catch (Exception e ){
						System.out.println("Exception Occoured "+e.getMessage());
					}	
					String tableRow = newoffset+","+fileName+","+termFreq;
					FileWriter fileWriter;
					try {
						fileWriter = new FileWriter("Posting.csv", true);
						fileWriter.write(tableRow);
						fileWriter.write("\n");
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					/** New Addition */ 
					offsetFreq.put(Integer.toString(newoffset), termFreq);
					offsetToDoc.put(Integer.toString(newoffset), file);

					if(offsetMap.containsKey(token)){
						List<String> offsets = offsetMap.get(token);
						offsets.add(Integer.toString(newoffset));
						offsetMap.put(token, offsets);
					} else {
						List<String> offsets = new ArrayList<String>();
						offsets.add(Integer.toString(newoffset));
						offsetMap.put(token, offsets);
					}


					newoffset++;
				}
				token.replace(",","");
				String dictonaryRow = token+","+globalCollection.get(token)+","+tokenDocument.get(token).size()+","+oldoffset;
				FileWriter fileWriter;
				try {
					//offsetMap.p
					fileWriter = new FileWriter("Dictonary.csv", true);
					fileWriter.write(dictonaryRow);
					fileWriter.write("\n");
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				oldoffset =  newoffset;
			}
		}
	}


	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/


	private boolean processSubdir(File directory){
		File[] subdir = directory.listFiles();
		for(int index = 0; index < subdir.length; index++){
			if(subdir[index].isDirectory()){
				File[] file = subdir[index].listFiles();
				for(int count = 0; count < file.length; count++){
					Tokenize ob;
					try {
						ob = new Tokenize(file[count]);
						ob.tokens();
						ob.Stem();
						ob.removeSingleLetters();
						List<String> allTokens = ob.getAllTokens();
						for(int i = 0; i < allTokens.size(); i++){
							if(allTokens.get(i).length() > 1) {
								if(globalCollection.containsKey(allTokens.get(i))){
									globalCollection.put(allTokens.get(i), globalCollection.get(allTokens.get(i)) + 1);
								} else {
									globalCollection.put(allTokens.get(i), 1 );	
								}
							}
						}
					} catch (IOException e) {
						//e.printStackTrace();
						return false;
					}
				}
			}
		}
		return true;
	}

	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	private boolean docTable(File directory){
		File[] subdir = directory.listFiles();
		int fileCounter = 0;
		for(int index = 0; index < subdir.length; index++){
			if(subdir[index].isDirectory()){
				File[] file = subdir[index].listFiles();
				for(int count = 0; count < file.length; count++){
					Tokenize ob;
					try {
						ob = new Tokenize(file[count]);
						ob.tables(file[count].toString()); 
					} catch (IOException e) {
						//e.printStackTrace();
						return false;
					}
				}
			}
		}
		return true;
	}


	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	private void totalToken(Map<String, List<String> > myMap){
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("Total.csv");
			fileWriter.write(myMap.size()+",");
			fileWriter.write("\n");
			fileWriter.close();
			totalToken = myMap.size();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/


	private void printtokenDocs(Map<String, List<String>> myMap){
		for (Map.Entry<String,List<String> > entry : myMap.entrySet()) {
			String key = entry.getKey();
			List<String> docs =entry.getValue();
			for(int i =0 ;i < docs.size();i++){
				System.out.println("Key is "+key+ " Value is "+docs.get(i));
			}
		}
	}

	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	private void printHash(Map<String, Integer> myMap){
		System.out.println("Hash Printing Here " +myMap.size());
		for (Map.Entry<String,Integer> entry : myMap.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			System.out.println("Key is "+key+ " Value is "+value);
		}
	}

	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	private void setDoc(){
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("DocsTable.csv"));
			String line = null;
			while ((line = br.readLine()) != null) {
				String []words = line.split(",");
				String docName =words[0].trim();
				docHeadline.put(docName, words[2]);
				docSnippet.put(docName, words[3]);			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

	}


	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	private void init(String directoryName){
		System.out.println("__init__");
		try{
			File dir = new File(directoryName);
			if(!dir.isDirectory()){
				System.out.println("Something Wrong with File Path");
			} else {
				System.out.println("Please Wait File Processing .... "+dir);
				if(processSubdir(dir)){
					System.out.print("Doc Table Creation .... "+dir);
					if(docTable(dir)) {
						System.out.print("Done!!! \nDictonary Creation .... ");
						Dictonary(dir);
						System.out.print("Done!!! \nPosting Processing .... ");
						Posting();
						System.out.print("Done!!! \nTotal Token Processing .... ");
						totalToken(tokenDocument);
						setDoc();
					} else 
						System.out.println("Something Wrong with the processing ");
				} else 
					System.out.println("Something Wrong with the processing ");
			}
			System.out.println("Done!!! \nInitialization Complete !!!\nPlease find the respective files in your project file path ");
		} catch (Exception e)
		{}
	}


	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	/* List all the files which has the corresponding token HashMap<token, Filename>  for a particular query
	 * for each token, scan each file for the terms and calculate the rank
	 */

	public void Search(String query){
		Tokenize searchToken;
		try {
			searchToken = new Tokenize();
			List<String> queryToken = searchToken.queryTokens(query);
			HashMap<String, List<String> > documents = new HashMap<String, List<String> >();

			for(int index = 0; index < queryToken.size(); index++){
				String token = queryToken.get(index);
				//System.out.println("Search Token is "+token);
				if(tokenDocument.containsKey(token)){
					List<String> docs = tokenDocument.get(token);
					for(int i = 0; i< docs.size();i++){
						if(documents.containsKey(docs.get(i))){ // If document been already been considered 
							List<String> documentsToken = documents.get(docs.get(i));
							documentsToken.add(token);
							documents.put(docs.get(i), documentsToken);
						} else { 
							List<String> documentsToken =  new ArrayList<String>();
							documentsToken.add(token);
							documents.put(docs.get(i), documentsToken);
						}
					}
				} else {
					//System.out.println("Token "+token+ " Not Found " );
				}
			}
			//System.out.println("**RANK PRINTING******\n\n\n");
			Map<Double, String> documentList = Rank(documents); // Sort this docList




			if(documentList.isEmpty()){
				System.out.println("NO RESULT \n\n\n");	
			} else {
				String search = "QUERY:->"+query+"\n*******************";
				for(Entry<Double, String> entry : documentList.entrySet()){
					Double rank = entry.getKey();
					String document = entry.getValue().trim(); 

					String snipett = docSnippet.get(document);
					String headline = docHeadline.get(document);

					//String tableRow = newoffset+","+fileName+","+termFreq;

					search += "\nDOCUMENT:-> "+document+"\nHEADLINE :-> "+headline+"\nSNIPETT:-> "+snipett+"\nRANK:-> "+rank+"\n\n";
					// Have to Pull snipett and headline
				}
				FileWriter fileWriter = new FileWriter("output.txt", true);
				fileWriter.write(search);
				fileWriter.write("\n");
				fileWriter.close();
				System.out.println(search);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}	
	}

	/********************************************************
	 * 
	 * METHOD NAME : 
	 * INPUT 	   : 
	 * RETURNS	   : 
	 * PURPOSE     :  
	 *
	 ********************************************************/

	public Map<Double, String> Rank(HashMap<String, List<String>> documents){ 

		TreeMap<Double, String> rank = new TreeMap<Double, String>();

		for(Entry<String, List<String>> entry : documents.entrySet()){
			String document = entry.getKey();
			List<String> listOfToken = entry.getValue();
			double rankValue = 0.0;
			for(int i = 0; i< listOfToken.size(); i++){
				String token = listOfToken.get(i);
				List<String> offset = offsetMap.get(token);
				for(int docID = 0 ; docID < offset.size();docID++){
					String docOffset = offset.get(docID);
					if(offsetToDoc.get(docOffset).equals(document)){
						int tf = offsetFreq.get(offset.get(docID)); // Term Frequency
						int wordCount = documentTerms.get(document);

						int collectionFreq = globalCollection.get(token);


						double first = (0.9 * ((double) tf / (double)wordCount));
						double second = 1 / ( 0.1 * ((double)totalToken  / (double) collectionFreq));

						double toLog = first + second;
						double calculatedRank = ((double) Math.log(toLog) / (double) Math.log(2));
						rankValue += calculatedRank;						
					}
				}
			}  
			rank.put(rankValue, document);
		}
		return rank;
	}

	/********************************************************
	 * 
	 * METHOD NAME : main 
	 * INPUT 	   : <Command Line Arguments>
	 * RETURNS	   : None
	 * PURPOSE     : Driver method
	 *
	 ********************************************************/

	public static void main(String a[]){
		Processor processosor = new Processor();
		Scanner scanner = new Scanner(System.in);
		if(a.length == 0){
			System.out.println("Missing Parameter");
		} else {
			processosor.init(a[0]);
			while(true){
				System.out.println("PLEASE ENTER YOUR SEARCH QUERY ");
				String input = scanner.nextLine();
				if(input.toLowerCase().equals("exit")){
					break;
				}
				//System.out.println(input);
				processosor.Search(input);
			}
		}
	}
}