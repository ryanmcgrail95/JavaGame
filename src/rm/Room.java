package rm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cont.GameController;
import cont.Log;
import ds.StringExt2;
import fl.FileExt;
import gfx.GL;
import object.primitive.Updatable;
import paper.CharacterPM;
import resource.Loadbar;
import resource.Resource;
import resource.model.Model;
import script.Script;
import script.exception.ChompException;
import script.exception.ParseException;

public class Room {
	private final static String BASE_DIRECTORY = "Resources/Rooms/";
	private static String nextRoom = "", curRoom = "Toad Town Center", prevRoom;
	private static List<Resource> resourceList = new ArrayList<Resource>();
	private static boolean isLoading;
	
	private String prevMusic;
	

	private Room() {}

	
	public static boolean isLoading() {
		return isLoading;
	}
	
	public static boolean inBattle() {
		return curRoom == "Battle";
	}
	
	public static void resetRoom() {
		changeRoom(curRoom);
	}
	public static void revertRoom() {
		changeRoom(prevRoom);
	}
	public static void changeRoom(String newRoomName) {
		nextRoom = newRoomName;
	}
	
	
	
	public static void getResourceList(String roomName, List<Resource> resources) {
		String path = BASE_DIRECTORY + roomName + "/resources.dat";
		
		StringExt2 fileStr = new StringExt2(FileExt.readFile2String(path));
		StringExt2 curLine = new StringExt2();
		
		String[] words;
		int wordNum;
		
		Model curModel;
		List<Model> curModels = new ArrayList<Model>();
		
		float scale;
		float rX, rY, rZ;
		
		float specTime = 0;

		while(!fileStr.isEmpty()) {
			curLine.set(fileStr.munchLine());
			curLine.munchSpace();

			curLine.println();
			
			if(curLine.startsWith("//") || curLine.isWhiteSpace() || curLine.isEmpty())		
				continue;

			
			words = curLine.split(' ');
			
			wordNum = words.length;

			switch(words[0]) {
				case "Model:":
					curModels.clear();
					for(int i = 1; i < wordNum; i++) {
						curModel = Model.get(words[i], false);
						resources.add(curModel);
						curModels.add(curModel);
					}
					break;
				case "Character:":
					for(int i = 1; i < wordNum; i++)
						resources.add(CharacterPM.getCharacter(words[i], false));
					break;
					
					
				// VARIABLES
				case "scale:":
					scale = Float.parseFloat(words[1]);
					for(Model m : curModels)
						m.scale(scale);
					break;
				case "rotation:":
					rX = Float.parseFloat(words[1]);
					rY = Float.parseFloat(words[2]);
					rZ = Float.parseFloat(words[3]);
						
					for(Model m : curModels) {
						m.rotateX(rX);
						m.rotateY(rY);
						m.rotateZ(rZ);
					}

					break;
								
				default:
			}
		}
		
		curModels.clear();		
	}
	
	public static void instantiateRoom(String roomName) {
		String path = BASE_DIRECTORY + roomName + "/layout.dat";

		try {
			Script.exec(GL.memory, FileExt.readFile2String(path));
		}
		catch(ParseException e) {
			e.printStackTrace();
			GameController.end();
		}
		
		/*if(true)
			return;
		StringExt curLine = new StringExt();
				
		String[] words;
		int wordNum;
		
		Map<String, Double> varMap = new HashMap<String, Double>();
		
		Stack<Updatable> objectStack = new Stack<Updatable>();
			Updatable curObj;
			ActorPM topActor = null;
			Positionable topPos = null;
			
		float x1, y1, z1, x2, y2, z2;
		
		while(!fileStr.isEmpty()) {
			curLine.set(fileStr.chompLine());
						
			if(curLine.startsWith("//")) {
				System.out.println("COMMENT");
				continue;
			}
			else if(curLine.isWhiteSpace()) {
				System.out.println("WHITE SPACE");
				continue;
			} 
			else if(curLine.isEmpty()) {
				System.out.println("EMPTY");
				continue;
			}
			
			curLine.chompWhiteSpace();
			words = curLine.split(' ');
			wordNum = words.length;
			
			switch(words[0]) {
				case "Player":
					curObj = PlayerPM.create();

					if(wordNum > 1)
						if(words[1].equals("{")) {
							objectStack.push(curObj);
							if(curObj instanceof ActorPM)
								topActor = (ActorPM) curObj;
							if(curObj instanceof Positionable)
								topPos = (Positionable) curObj;
						}
					break;
				
				case "Floor":
					x1 = parseValue(words[2], varMap);
					y1 = parseValue(words[3], varMap);
					x2 = parseValue(words[4], varMap);
					y2 = parseValue(words[5], varMap);
					z1 = parseValue(words[6], varMap);
										
					curObj = new Floor(x1,y1,x2,y2,z1, null);
					break;
					
				case "}":
					objectStack.pop();
					if(!objectStack.isEmpty())
						curObj = objectStack.peek();
					else
						curObj = null;
					if(curObj != null) {
						if(curObj instanceof ActorPM)
							topActor = (ActorPM) curObj;
						if(curObj instanceof Positionable)
							topPos = (Positionable) curObj;
					}
					break;
					
				
				// VARIABLES
				case "character:":
					topActor.setCharacter(words[1]);
					break;
				case "position:":
					topPos.setPos(
						parseValue(words[1], varMap),
						parseValue(words[2], varMap),
						parseValue(words[3], varMap));
					break;
					
				default:
			}
		}*/
	}

	private static float parseValue(String str, Map<String, Double> varMap) {
		return Float.parseFloat(str);
	}


	public static void change() {
		if(nextRoom == "")
			return;
			
		isLoading = true;
		prevRoom = curRoom;
		curRoom = nextRoom;
		nextRoom = "";
				
		List<Resource> newResourceList = new ArrayList<Resource>(),
			unloadResourceList = new ArrayList<Resource>(),
			loadResourceList = new ArrayList<Resource>();
		
				
		Updatable.transition();
		
		// Get New Resource List
		getResourceList(curRoom, newResourceList);
		
		boolean isInside;
		
		// Determine Unload List
		for(Resource r : resourceList) {
			isInside = false;
			for(Resource rr : newResourceList)
				if(r == rr || r.getFileName().equals(rr.getFileName())) {
					isInside = true;
					break;
				}
			if(!isInside)
				unloadResourceList.add(r);
		}
		
		// Determine Load List
		for(Resource r : newResourceList) {
			isInside = false;
			for(Resource rr : resourceList)
				if(r == rr || r.getFileName().equals(rr.getFileName())) {
					isInside = true;
					break;
				}
			if(!isInside)
				loadResourceList.add(r);
		}
		
		
		// Unload All Unnecessary Resources
		Log.println(Log.ID.RESOURCE, true, "Unloading unneeded resources.");
		Loadbar bar = new Loadbar(unloadResourceList.size() + loadResourceList.size());
		for(Resource r : unloadResourceList) {
			r.unload();
			r.destroy();
			bar.step();
		}
		Log.println(Log.ID.RESOURCE, false, "");

		
		// Load New Resources
		for(Resource r : loadResourceList) {
			r.load();
			bar.step();
		}

		bar.destroy();
		
		// Clean Up Lists
		resourceList.clear();
		unloadResourceList.clear();
		loadResourceList.clear();
		resourceList = newResourceList;
		
		// Delete all Objects in Room
		Updatable.transition();
		
		// Set New Room
		instantiateRoom(curRoom);		
 
		isLoading = false;
	}
}
