package uk.ac.nott.cs.g53dia.demo;
import uk.ac.nott.cs.g53dia.library.*;
import java.util.*;
import java.util.Arrays;
import java.nio.file.Files;
/**
 * A simple example Tanker
 *
 * @author Julian Zappala
 */
/*
 *
 * Copyright (c) 2011 Julian Zappala
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class DemoTanker extends Tanker {

    private ArrayList<int[]> seenWells = new ArrayList<int[]>();
    private ArrayList<int[]> seenStations = new ArrayList<int[]>();
    private ArrayList<int[]> seenTasks = new ArrayList<int[]>();

    private int tankPosX = 0;
    private int tankPosY = 0;

    public DemoTanker() {

    }

    /*
     * The following is a very simple demonstration of how to write a tanker. The
     * code below is very stupid and pretty much randomly picks actions to perform.
     */
    public Action senseAndAct(Cell[][] view, long timestep) {
        initialSeenThings(view);
        System.out.println(seenWells.size());
        System.out.println(seenStations.size());
        System.out.println(seenTasks.size());
    	// If fuel tank is low and not at the fuel pump then move towards the fuel pump
        if ((getFuelLevel() <= MAX_FUEL/2)) {
			if(getCurrentCell(view) instanceof FuelPump){
				return new RefuelAction();
			} else {
				return new MoveTowardsAction(FUEL_PUMP_LOCATION);
			}
        } else {
            // otherwise, move randomly
            return new MoveAction((int)(Math.random() * 8));
        }
    }

    private void initialSeenThings(Cell[][] view){
        for(int i = 0;i < view.length;i++){
            for(int j = 0;j < view[i].length;j++){
                // Get focused position's coordinate
                int[] focusedPos = new int[]{j+tankPosX-25, i+tankPosY-25};
                // int[] focusedPos = new int[2];
                // if(25 <= i && i <= 50){
                //     if(0 <= j && j < 25){
                //         // Second quadrant
                //         focusedPos[0] = j - 25 + tankPosX;
                //         focusedPos[1] = i - 25 + tankPosY;
                //     } else if(25 <= j && j <= 50){
                //         // First quadrant
                //         focusedPos[0] = j - 25 + tankPosX;
                //         focusedPos[1] = i - 25 + tankPosY;
                //     }
                // } else if(0 <= i && i < 25){
                //     if(0 <= j && j< 25){
                //         // Third quadrant
                //         focusedPos[0] = j - 25 + tankPosX;
                //         focusedPos[1] = i - 25 + tankPosY;
                //     } else if(25 <= j && j <= 50){
                //         // Fourth quadrant
                //         focusedPos[0] = j - 25 + tankPosX;
                //         focusedPos[1] = i - 25 + tankPosY;
                //     }
                // }

                // if (Math.max(Math.abs(focusedPos[0]), Math.abs(focusedPos[1])) <= 50){
                    if (view[i][j] instanceof Station) {
    					if (isInList(seenStations, focusedPos) == -1){
    						seenStations.add(focusedPos);
                            System.out.println("Station:" + "(" + focusedPos[0] + "," + focusedPos[1]+") // "+i+','+j);
    					}

    					Station focusedSta = (Station) view[i][j];
    					if (focusedSta.getTask() != null){
    						int[] taskDetails = new int[]{focusedPos[0], focusedPos[1], focusedSta.getTask().getWasteAmount()};
    						if (isInList(seenTasks, taskDetails) == -1){
    							seenTasks.add(taskDetails);
    						}
    					}
    				} else if (view[i][j] instanceof Well && isInList(seenWells, focusedPos) == -1) {
    					seenWells.add(focusedPos);
                        System.out.println("Well:" + "(" + focusedPos[0] + "," + focusedPos[1]+") // "+i+','+j);
    				} else {
    					continue;
    				}
                // }


            }
        }
    }

    private int isInList(ArrayList<int[]> seenList, int[] focused){
        for (int i = 0; i < seenList.size(); i++){
            if (Arrays.equals(seenList.get(i), focused)){
                return i;
            }
        }
        return -1;
    }

    private 
}
