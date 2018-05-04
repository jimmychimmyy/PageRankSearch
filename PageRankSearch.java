import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.Collection;
import java.util.Random;

public class PageRankSearch {

	public static String LINK_TITLES = "linkString.txt";
	public static String LINK_NUMBERS = "linkNumber.txt";
	public static String TEST_LINK_NUMBERS = "testLinkNumber.txt";
	public static String TEST_LINK_TITLES = "testLinkString.txt";
	//http://pr.efactory.de/e-pagerank-algorithm.shtml

	// this can be adjusted
	public static final double DAMPING_FACTOR = 0.85;

	private static HashMap<Integer, Double> pageranks;

	private static ArrayList<String> webPages;
	private static HashMap<Integer, ArrayList<Integer>> incomingLinks;
	private static HashMap<Integer, ArrayList<Integer>> outgoingLinks;

	public static void main(String[] args) {

		System.out.println("Hello PageRankSearch");

		PageRankSearch pageRankSearch = new PageRankSearch();
		pageRankSearch.webPages = new ArrayList<String>();
		pageRankSearch.incomingLinks = new HashMap<Integer, ArrayList<Integer>>();
		pageRankSearch.outgoingLinks = new HashMap<Integer, ArrayList<Integer>>();
		pageRankSearch.pageranks = new HashMap<Integer, Double>();

		pageRankSearch.readWebPagesFromTxt();
		System.out.println("Size of webPages: " + pageRankSearch.webPages.size());

		pageRankSearch.readIncomingLinksFromTxt();
		/*
		System.out.println("Size of incomingLinks: " + pageRankSearch.incomingLinks.size());
		System.out.println("0th key of incomingLink: " + pageRankSearch.incomingLinks.keySet().toArray()[0]);
		System.out.println("0th value of incomingLink: " + pageRankSearch.incomingLinks.values().toArray()[0]);
		System.out.println("1st value of incomingLink: " + pageRankSearch.incomingLinks.values().toArray()[1]);
		System.out.println("2nd value of incomingLink: " + pageRankSearch.incomingLinks.values().toArray()[2]);
		*/


		pageRankSearch.computeOutgoingLinksUsingIncoming();
		/*
		System.out.println("Size of outgoingLinks: " + pageRankSearch.outgoingLinks.size());
		System.out.println("0th key of outgoingLink: " + pageRankSearch.outgoingLinks.keySet().toArray()[0]);
		System.out.println("0th value of outgoingLink: " + pageRankSearch.outgoingLinks.values().toArray()[0]);
		System.out.println("1st value of outgoingLink: " + pageRankSearch.outgoingLinks.values().toArray()[1]);
		System.out.println("2nd value of outgoingLink: " + pageRankSearch.outgoingLinks.values().toArray()[2]);
		*/


		pageRankSearch.initializePagerank();
		Random rand = new Random();
		int rand_index = rand.nextInt(pageRankSearch.incomingLinks.size());

		for (int i = 0; i < 11; i++) {
			System.out.println(i + "th iteration of " + rand_index + " of pageranks: " + pageRankSearch.pageranks.get(rand_index));
			pageRankSearch.computePagerank();
		}

	}

	private void readWebPagesFromTxt() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(LINK_TITLES));
			//BufferedReader bufferedReader = new BufferedReader(new FileReader(TEST_LINK_TITLES));
			String line = "";

			while ((line = bufferedReader.readLine()) != null) {
				Pattern pattern = Pattern.compile("<title>(.+?)</title>", Pattern.MULTILINE);
				Matcher matcher = pattern.matcher(line);

				while (matcher.find()) {
					String title = matcher.group().replaceAll("\\<.*?>","");
					this.webPages.add(title);
				}
			}

			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readIncomingLinksFromTxt() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(LINK_NUMBERS));
			//BufferedReader bufferedReader = new BufferedReader(new FileReader(TEST_LINK_NUMBERS));
			String line = "";

			while ((line = bufferedReader.readLine()) != null) {
				String[] subStrings = line.split("\\s+"); // subStrings[0] is our key
				ArrayList<Integer> links = new ArrayList<Integer>();
				for (int i = 1; i < subStrings.length; i++) {
					links.add(Integer.valueOf(subStrings[i]));
				}
				this.incomingLinks.put(Integer.valueOf(subStrings[0]), links);


				ArrayList<Integer> link = new ArrayList<Integer>();
				link.add(Integer.valueOf(-1));
				this.outgoingLinks.put(Integer.valueOf(subStrings[0]), link);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void computeOutgoingLinksUsingIncoming() {
		for (int i = 0; i < this.incomingLinks.size(); i++) {
			if (this.incomingLinks.get(i) != null) {
				ArrayList<Integer> links = this.incomingLinks.get(i);
				//System.out.println(links);

				// in first iteration links == [2]

				if (links != null) {
					for (int j = 0; j < links.size(); j++) {
						ArrayList<Integer> link = new ArrayList<Integer>();
						if (this.outgoingLinks.get(j) != null) {
							if (this.outgoingLinks.get(j).get(0) == -1) {
								link = new ArrayList<Integer>();
							} else {
								link = this.outgoingLinks.get(j);		
							}
						}
						//System.out.println(link);
						link.add(Integer.valueOf(i));
						this.outgoingLinks.put(Integer.valueOf(links.get(j)), link);
					}
				}
			}
		}
	}

	private void initializePagerank() {
		// start by initializing every value in pagerank to 1
		for (int i = 0; i < this.incomingLinks.size(); i++) {
			this.pageranks.put(Integer.valueOf(i), 1.0);
		}
		System.out.println("Value of first key-value pair in pageranks: " + pageranks.get(0));
	}

	private void computePagerank() {
		for (int i = 0; i < this.pageranks.size(); i++) {
			double pageRankScore = (1 - DAMPING_FACTOR) + DAMPING_FACTOR * (getIncomingOutgoingPageRankScores(i));
			this.pageranks.put(Integer.valueOf(i), pageRankScore);
		}
	}

	private double getIncomingOutgoingPageRankScores(int page) {
		Double result = 0.0;
		ArrayList<Integer> incoming = this.incomingLinks.get(page); // get list of incoming links
		if (incoming != null) {
			for (int i = 0; i < incoming.size(); i++) {
				// get ranks associated with each incoming link
				// divide by the following:
				// get number of outgoing links of each incoming link
				int numberOfOutgoingLinks = this.outgoingLinks.get(incoming.get(i)).size();

				if (numberOfOutgoingLinks != 0 && this.pageranks.get(incoming.get(i)) != null) {
					//System.out.println(this.pageranks.get(incoming.get(i)));
					Double rank = this.pageranks.get(incoming.get(i)) / numberOfOutgoingLinks;
					result += rank;
				}
			}
		}
		return result;
	}

}