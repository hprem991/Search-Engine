import java.lang.*;
import java.io.*;
import org.w3c.dom.*;
import java.util.*;

/*****************************************************************************
 * 
 * @author PREM KRISHNA CHETTRI
 * CLASS NAME  : Tokenize
 * PURPOSE     : Read a file and convert each token for information retrieval
 *
 *****************************************************************************/


class Tokenize {
	FileWriter fileWriter;
	private File filename;
	private int noOfOpeningTags;
	List <String> tokens;

	Tokenize() throws IOException {
		noOfOpeningTags = 0;
	}

	/********************************************************
	 * 
	 * METHOD NAME : Constructor 
	 * INPUT 	   : None
	 * RETURNS	   : None
	 * PURPOSE     : Tokenize an input file
	 *
	 ********************************************************/
	Tokenize(File file) throws IOException {
		filename = file;
		noOfOpeningTags = 0;
		tokens = new ArrayList<String>();
	}

	/********************************************************
	 * 
	 * METHOD NAME : tables 
	 * INPUT 	   : None
	 * RETURNS	   : None
	 * PURPOSE     : Convert an input filename into relevant tokens
	 *
	 ********************************************************/
	public void tables(String counter){
		BufferedReader br = null;
		String fileStr = null;
		boolean headlines = false, snipett = true, text = false;
		String firstSentence="", headline="";
		int wordsCount = 0, snippitCount = 0;


		try {
			br = new BufferedReader(new FileReader(filename));

			while ((fileStr = br.readLine()) != null) {
				if(headlines && !fileStr.contains("</HEADLINE>")){
					headline = fileStr.trim();
				}

				if(snipett && text && !fileStr.contains("</TEXT>")){
					String sentence[] = fileStr.split("\\s+");
					snippitCount += sentence.length;
					
					if(snippitCount <= 40){
						firstSentence += fileStr.trim();
					} else {
						snipett = false;
					}
				}
				// Setting Flag 
				if(fileStr.contains("<TEXT>")){
					text = true;
				} else if(fileStr.contains("</TEXT>")) {
					text = false;
				} else if(fileStr.contains("<HEADLINE>")){
					headlines = true;
				} else if(fileStr.contains("</HEADLINE>")) {
					headlines = false;
				}

				String str[] = fileStr.split("-|\\s+");

				for(int index = 0 ; index < str.length; index++){
					if(avoidToken(str[index])) 
						continue;
					if(!postProcessing(str[index].toLowerCase().trim()).isEmpty())
						wordsCount++;
				}				
			}

			if(headline.contains(",")){
				headline = headline.replace(",", "");
				if(headline.contains("<P>"))
					headline = headline.replace("<P>", "");
				if(headline.contains("<\\P>"))
					headline = headline.replace("<\\P>", "");
			}
			if(firstSentence.contains(",")){
				firstSentence = firstSentence.replace(",", "");
				if(firstSentence.contains("<P>"))
					firstSentence = firstSentence.replace("<P>", "");
				if(firstSentence.contains("<\\P>"))
					firstSentence = firstSentence.replace("<\\P>", "");
			}
			
			FileWriter fileWriter = new FileWriter("DocsTable.csv", true);
			fileWriter.write(counter+"   ,   "+wordsCount+"   ,   "+headline+"   ,    "+firstSentence+"\n");
			fileWriter.close();

		} catch (IOException e) {
			//e.printStackTrace();
			//System.out.println("Exception Occoured "+e.getMessage());
		} 
	}

	/********************************************************
	 * 
	 * METHOD NAME : tokens 
	 * INPUT 	   : None
	 * RETURNS	   : None
	 * PURPOSE     : Convert an input filename into relevant tokens
	 *
	 ********************************************************/
	public void tokens(){
		BufferedReader br = null;
		String fileStr = null;

		try {
			br = new BufferedReader(new FileReader(filename));
			while ((fileStr = br.readLine()) != null) {
				String str[] = fileStr.split("-|\\s+");
				for(int index = 0 ; index < str.length; index++){
					if(!avoidToken(str[index])) {
						String token = removeSpecialChar(postProcessing(str[index].toLowerCase().trim()));
						try {
							if(!token.isEmpty() && (token.length() > 1)){
								tokens.add(token);	
							}
						} catch (Exception e){
							System.out.println("Exception "+e.getMessage());
						}
					}
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
			//System.out.println("Exception Occoured "+e.getMessage());
		} 
	}

	/********************************************************
	 * 
	 * METHOD NAME : queryTokens 
	 * INPUT 	   : None
	 * RETURNS	   : None
	 * PURPOSE     : Convert an input filename into relevant tokens
	 *
	 ********************************************************/
	public List<String> queryTokens(String query){
		List<String> queryTokens = new ArrayList<String>();
		try {
			String str[] = query.split("-|\\s+");
			for(int index = 0 ; index < str.length; index++){
				if(!avoidToken(str[index])) {
					String token = removeSpecialChar(postProcessing(str[index].toLowerCase().trim()));
					try {
						if(!token.isEmpty() && (token.length() > 1)){
							queryTokens.add(token);	
						}
					} catch (Exception e){
						System.out.println("Exception "+e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Exception Occoured "+e.getMessage());
		} 
		return 	queryTokens;
	}



	/********************************************************
	 * 
	 * METHOD NAME : avoidToken 
	 * INPUT 	   : token as a String 
	 * RETURNS	   : true if the token is to be avoided as valid token
	 * PURPOSE     : To verify if the token is valid for information 
	 *               retrieval or not
	 *
	 ********************************************************/
	private boolean avoidToken(String str){
		return (str.contains("<")||str.contains(">")||
				str.equals("a")||str.equals("and")||
				str.equals("in")||str.equals("by")||
				str.equals("from")||str.equals("of")||
				str.equals("the")||str.equals("with")||
				str.equals("an")|| (str.length() == 1) ||
				str.isEmpty()
				);
	}


	/********************************************************
	 * 
	 * METHOD NAME : postProcessing 
	 * INPUT 	   : token as a String
	 * RETURNS	   : modified token as a String
	 * PURPOSE     : To process the given token for information
	 *				 retrieval.
	 *
	 ********************************************************/
	private String postProcessing(String str){
		if(needProcessing(str)){
			String temp = null;
			if(startCheck(str)){
				temp = str.substring(1, str.length());
			} else if(endCheck(str)){
				temp = str.substring(0, str.length()-1);
			} else if(endCharSpaceCheck(str)){
				temp = str.trim().substring(0, (str.trim().length()-1));
			} else if(containsCheck(str)){
				temp = str.replace("'", "");
			}
			return postProcessing(temp);
		}	
		return str;
	}

	/********************************************************
	 * 
	 * METHOD NAME : isNumeric 
	 * INPUT 	   : token as a String
	 * RETURNS	   : modified token as a String
	 * PURPOSE     : To process the given token for information
	 *				 retrieval.
	 *
	 ********************************************************/

	public static boolean isNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}
	/********************************************************
	 * 
	 * METHOD NAME : removeSpecialChar 
	 * INPUT 	   : token as a String
	 * RETURNS	   : modified token as a String
	 * PURPOSE     : To process the given token for information
	 *				 retrieval.
	 *
	 ********************************************************/
	private String removeSpecialChar(String str){
		String modified = str;
		if(str.contains(":")){
			modified = str.replace(":", "");
			if(isNumeric(modified)) {
				modified = str.replace(":", "/");
			} 
		} 
		if(str.contains(",")){
			modified = str.replace(",", "");
		} 
		if(str.contains("?")){
			modified = str.replace("?", "");
		} 
		if(str.contains("!")){
			modified = str.replace("!", "");
		} 
		if(str.contains(";")){
			modified = str.replace(";", "");
		} 
		if(str.contains("[")){
			modified = str.replace("[", "");
		} 
		if(str.contains("]")){
			modified = str.replace("]", "");
		} 
		if(str.contains("<")){
			modified = str.replace("<", "");
		} 
		if(str.contains(">")){
			modified = str.replace(">", "");
		} 
		if(str.contains("@")){
			modified = str.replace("@", "");
		} 
		if(str.contains("&")){
			modified = str.replace("&", "");
		} 
		if(str.contains("~")){
			modified = str.replace("~", "");
		} 
		if(str.contains("#")){
			modified = str.replace("#", "");
		} 
		if(str.contains("^")){
			modified = str.replace("^", "");
		} 
		if(str.contains("%")){
			modified = str.replace("%", "");
		} 
		if(str.contains("$")){
			modified = str.replace("$", "");
		} 
		if(str.contains("*")){
			modified = str.replace("*", "");
		} 
		if(str.contains("(")){
			modified = str.replace("(", "");
		} 
		if(str.contains(")")){
			modified = str.replace(")", "");
		} 
		if(str.contains("_")){
			modified = str.replace("_", "");
		} 
		if(str.contains("+")){
			modified = str.replace("+", "");
		} 
		if(str.contains("{")){
			modified = str.replace("{", "");
		} 
		if(str.contains("}")){
			modified = str.replace("}", "");
		} 
		if(str.contains("|")){
			modified = str.replace("|", "");
		} 
		if(str.contains("\"")){
			modified = str.replace("\"", "");
		} 
		if(str.contains("`")){
			modified = str.replace("`", "");
		} 
		return modified;
	}

	/********************************************************
	 * 
	 * METHOD NAME : startCheck 
	 * INPUT 	   : token as a String
	 * RETURNS	   : true if the token starts with certain pattern
	 * PURPOSE     : To check if the token starts with unwanted
	 *               pattern.
	 *
	 ********************************************************/
	private boolean startCheck(String str){
		return (str.toString().startsWith("(") ||
				str.toString().startsWith("[") ||
				str.toString().startsWith("\"") ||
				str.toString().startsWith("\'"));
	}


	/********************************************************
	 * 
	 * METHOD NAME : endCheck 
	 * INPUT 	   : token as a String
	 * RETURNS	   : true if the token starts with certain pattern
	 * PURPOSE     : To check if the token ends with unwanted 
	 *               pattern.
	 *
	 ********************************************************/
	private boolean endCheck(String str){
		return (str.toString().endsWith(")") ||
				str.toString().endsWith("]") ||
				str.toString().endsWith("\"") ||
				str.toString().endsWith("\'"));
	}


	/********************************************************
	 * 
	 * METHOD NAME : endCharSpaceCheck 
	 * INPUT 	   : token as a String
	 * RETURNS	   : true if the token end with certain character
	 *   			 and space. 
	 * PURPOSE     : to check if the token ends with unwanted 
	 * 				 pattern 
	 *
	 ********************************************************/
	private boolean endCharSpaceCheck(String str){
		return (str.toString().endsWith("?") ||
				str.toString().endsWith("!") ||
				str.toString().endsWith(",") ||
				str.toString().endsWith(".") ||
				str.toString().endsWith(";") ||
				str.toString().endsWith(":"));
	}


	/********************************************************
	 * 
	 * METHOD NAME : containsCheck 
	 * INPUT 	   : token as a String
	 * RETURNS	   : true if the token contains whitespaces
	 * PURPOSE     : to check if the token contains white spaces
	 *
	 ********************************************************/
	private boolean containsCheck(String str){
		return (str.contains("'"));
	}


	/********************************************************
	 * 
	 * METHOD NAME : needProcessing 
	 * INPUT 	   : token as a String
	 * RETURNS	   : true if the token need modification 
	 * PURPOSE     : to check if the token needs modification.
	 *
	 ********************************************************/
	private boolean needProcessing(String str){
		return startCheck(str) || endCheck(str) || endCharSpaceCheck(str) || containsCheck(str);
	}


	/********************************************************
	 * 
	 * METHOD NAME : Stem 
	 * INPUT 	   : None
	 * RETURNS	   : None
	 * PURPOSE     : To modify the enlisted token as according 
	 * 			     to the stemming rule.
	 *
	 ********************************************************/
	public void Stem(){
		for(int index = 0; index < tokens.size(); index++){
			String token = tokens.get(index);
			if(token.endsWith("ies") && (!token.endsWith("aies")) && (!token.endsWith("eies"))){
				String temp = token.substring(0, token.length() - 3);
				tokens.remove(index);
				tokens.add(temp+"y");
			} else if(token.endsWith("es") && (!token.endsWith("aes")) && (!token.endsWith("ees")) && (!token.endsWith("oes"))){
				String temp = token.substring(0, token.length() - 2);
				tokens.remove(index);
				tokens.add(temp+"e");
			} else if(token.endsWith("s") && (!token.endsWith("us")) && (!token.endsWith("ss")
					&& (!token.equals("his")) && (!token.equals("this") && (!token.equals("is"))
							&& (!token.equals("was")) && (!token.equals("has"))))){
				String temp = token.substring(0, token.length() - 1);
				tokens.remove(index);
				tokens.add(temp);
			} else if(token.contains(" ")){
				tokens.remove(index);
			}
		}
		removeSingleLetters();
	}

	/********************************************************
	 * 
	 * METHOD NAME : removeSingleLetters 
	 * INPUT 	   : None
	 * RETURNS	   : None
	 * PURPOSE     : Removes any token that is left as single letter
	 * 				 token in the processed list.
	 *
	 ********************************************************/
	public void removeSingleLetters(){
		for(int index = 0; index < tokens.size(); index++){
			if(tokens.get(index).length() < 2){
				String s = tokens.get(index);
				tokens.remove(index);
			}
		}
		Collections.sort(tokens);
	}

	/********************************************************
	 * 
	 * METHOD NAME : depricatedProcessingCheck 
	 * INPUT 	   : token as a String
	 * RETURNS	   : true if the token need further processing
	 * PURPOSE     : to check if the token needs to be processed.
	 *
	 ********************************************************/
	private boolean depricatedProcessingCheck(String str){
		return str.toString().startsWith("(") || 
				str.toString().startsWith("{") ||
				str.toString().startsWith("[") ||
				str.toString().startsWith("\"") ||
				str.toString().startsWith("'") ||
				str.toString().endsWith(")") || 
				str.toString().endsWith("}") ||
				str.toString().endsWith("]") ||
				str.toString().endsWith("\"") ||
				str.toString().endsWith("'") ||
				(str.contains("? ")) ||
				(str.contains(", ")) ||
				(str.contains("! ")) ||
				(str.contains("; ")) ||
				(str.contains(": ")) ||
				(str.contains(". "));
	}


	/********************************************************
	 * 
	 * METHOD NAME : readToken 
	 * INPUT 	   : None
	 * RETURNS	   : None <Standard Output>
	 * PURPOSE     : to print the list of token from the list.
	 *
	 ********************************************************/
	public void readToken(){
		System.out.println("****************READING TOKENS **************");
		for(int index = 0; index < tokens.size(); index++){
			System.out.println(tokens.get(index));
		}
	}

	/********************************************************
	 * 
	 * METHOD NAME : writeToFile 
	 * INPUT 	   : filename as a String
	 * RETURNS	   : None
	 * PURPOSE     : To write the content to an output file.
	 *
	 ********************************************************/
	public void writeToFile(String fileName) throws IOException{
		fileWriter = new FileWriter(fileName, true);
		for(int index = 0; index < tokens.size(); index++){
			fileWriter.write(tokens.get(index));
			fileWriter.write("\n");
		}
		fileWriter.close();
	}

	/********************************************************
	 * 
	 * METHOD NAME : getAllTokens 
	 * INPUT 	   : 
	 * RETURNS	   : ListOf String as a token
	 * PURPOSE     : expose token collection
	 *
	 ********************************************************/
	public List<String> getAllTokens() {
		return tokens;
	}
}