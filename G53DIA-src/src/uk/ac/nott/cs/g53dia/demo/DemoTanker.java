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

    private ArrayList<int[]> initialWlakingAroundPoints = new ArrayList<int[]>();
    private boolean isInitialWlakingAround = true;
    private int initialWalkingAroundMonitor = 0;

    private ArrayList<int[]> seenWells = new ArrayList<int[]>();
    private ArrayList<int[]> seenStations = new ArrayList<int[]>();
    private ArrayList<int[]> seenTasks = new ArrayList<int[]>();
    private ArrayList<int[]> seenFuelpumps = new ArrayList<int[]>();

    private int tankPosX = 0;
    private int tankPosY = 0;

    public DemoTanker() {
        initialWlakingAroundPoints.add(new int[]{25,25});
        initialWlakingAroundPoints.add(new int[]{25,-25});
        initialWlakingAroundPoints.add(new int[]{0,0});
        initialWlakingAroundPoints.add(new int[]{-25,-25});
        initialWlakingAroundPoints.add(new int[]{-25,25});
        initialWlakingAroundPoints.add(new int[]{0,0});

        seenFuelpumps.add(new int[]{0,0});
    }

    /*
     * The following is a very simple demonstration of how to write a tanker. The
     * code below is very stupid and pretty much randomly picks actions to perform.
     */
    public Action senseAndAct(Cell[][] view, long timestep) {
        initialSeenThings(view);
        // System.out.println("("+seenStations.size()+","+seenWells.size()+","+seenTasks.size()+seenFuelpumps.size()+")");

        if(isInitialWlakingAround){
            return initialWalkingAround(view, timestep);
        }
    	// If fuel tank is low and not at the fuel pump then move towards the fuel pump
        else if ((getFuelLevel() <= MAX_FUEL/2)) {
            System.out.println("\\\\"+tankPosX);
            System.out.println("\\\\"+tankPosY);
			if(getCurrentCell(view) instanceof FuelPump){
				return new RefuelAction();
			} else {
				return new MoveTowardsAction(FUEL_PUMP_LOCATION);
			}
        } else {
            // otherwise, move randomly
            int a = (int)(Math.random() * 8);
            // System.out.println(tankPosX);
            // System.out.println(tankPosY);
            // tankMovement(4);
            return new MoveAction(a);
        }
    }

    private void initialSeenThings(Cell[][] view){
        for(int i = 0;i < view.length;i++){
            for(int j = 0;j < view[i].length;j++){
                // Get focused position's coordinate
                int[] focusedPos = new int[]{i+tankPosX-25, -j+tankPosY+25};
                int b = i+tankPosX-25;
                int c = -j+tankPosY+25;

                if (Math.max(Math.abs(focusedPos[0]), Math.abs(focusedPos[1])) <= 50){
                    if (view[i][j] instanceof Station) {
    					if (isInList(seenStations, focusedPos) == -1){
    						seenStations.add(focusedPos);
                            // System.out.println("(" + i + "," + j+")"   +   "(" + tankPosX + "," + tankPosY+")"   +   "(" + focusedPos[0] + "," + focusedPos[1]+")");
                            // System.out.println("-------"+b+"/"+c);
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
                        // System.out.println("(" + i + "," + j+")"   +   "(" + tankPosX + "," + tankPosY+")"   +   "(" + focusedPos[0] + "," + focusedPos[1]+")");
                        // System.out.println("-------"+tankPosX+'.'+tankPosY+"||||"+b+"/"+c);
    				} else if (view[i][j] instanceof FuelPump && isInList(seenFuelpumps, focusedPos) == -1) {
                        seenFuelpumps.add(focusedPos);
                    } else {
    					continue;
    				}
                }
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

    private void tankMovement(int dir){
        switch (dir){
            case 0:
                tankPosY++;
                break;
            case 1:
                tankPosY--;
                break;
            case 2:
                tankPosX++;
                break;
            case 3:
                tankPosX--;
                break;
            case 4:
                tankPosX++;
                tankPosY++;
                break;
            case 5:
                tankPosX--;
                tankPosY++;
                break;
            case 6:
                tankPosX++;
                tankPosY--;
                break;
            case 7:
                tankPosX--;
                tankPosY--;
                break;
        }
    }

    private Action initialWalkingAround(Cell[][] view, long timestep){
        if(initialWalkingAroundMonitor >= initialWlakingAroundPoints.size()){
            isInitialWlakingAround = false;
            return senseAndAct(view, timestep);
        }

        int[] targetPos = initialWlakingAroundPoints.get(initialWalkingAroundMonitor);
        if(targetPos[0] == tankPosX && targetPos[1] == tankPosY){
            initialWalkingAroundMonitor++;
            if(getCurrentCell(view) instanceof FuelPump){
                return new RefuelAction();
            } else {
                return senseAndAct(view, timestep);
            }
        } else {
            return moveTowardsPointsAction(view, targetPos);
        }
    }

    private Action moveTowardsPointsAction(Cell[][] view, int[] targetPos){
        int horizontalDifference = targetPos[0] - tankPosX;
        int verticalDifference = targetPos[1] - tankPosY;
        int verticalMovement, horizontalMovement;

        if(horizontalDifference > 0){
            horizontalMovement = 1;
        } else if (horizontalDifference == 0){
            horizontalMovement = 0;
        } else {
            horizontalMovement = -1;
        }

        if(verticalDifference > 0){
            verticalMovement = 1;
        } else if (verticalDifference == 0){
            verticalMovement = 0;
        } else {
            verticalMovement = -1;
        }

        int[] directionToGoArray = {horizontalMovement, verticalMovement};
        int directionToGo = 0;
        switch (horizontalMovement) {
            case 0:
                if(verticalMovement == 1){
                    directionToGo = 0;
                } else {
                    directionToGo = 1;
                }
                break;
            case 1:
                if(verticalMovement == 1){
                    directionToGo = 4;
                } else if (verticalMovement == 0) {
                    directionToGo = 2;
                } else {
                    directionToGo = 6;
                }
                break;
            case -1:
                if(verticalMovement == 1){
                    directionToGo = 5;
                } else if (verticalMovement == 0) {
                    directionToGo = 3;
                } else {
                    directionToGo = 7;
                }
                break;
        }

        int fuelLeft = getFuelLevel() - Math.max(Math.abs(horiChange), Math.abs(vertChange));
		int distanceToFuel = Math.max(Math.abs(pos[0]), Math.abs(pos[1]));
		if (fuelLeft < distanceToFuel){
			return getFuel(view);
		}
		else{
			return moveInDirection(movementIndex);
		}

        tankMovement(directionToGo);
        return new MoveAction(directionToGo);
    }

    private int getClosestIndexBetween(ArrayList<int[]> indicesList, int[] point){
        int furthestGoableDistance = 100;
        int closestIndex;
        for(int i = 0;i < indicesList.size();i++){
            int[] positionGoable = indicesList.get[i];
            int distanceBetween = Math.max(Math.abs(positionGoable[0] - point[0]),Math.abs(positionGoable[1] - point[1]));
            if(distanceBetween < furthestGoableDistance){
                furthestGoableDistance = distanceBetween;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    private Action workingProcedure(Cell[][] view){
        // Find nearest task going from current tank position
        int[] taskToGo = seenTasks.get[getClosestIndex(seenTasks, new int[]{tankPosX, tankPosY})];
        // int taskToGoX = taskToGo[0];
        // int taskToGoY = taskToGo[1];
        // int taskAmount = taskToGo[2];

        // Find nearest task going from current tank position
        int[] nearestFuelpumpGoingFromTask = seenFuelpumps.get[getClosestIndex(seenFuelpumps, taskToGo)];

        int distanceBetweenTankAndTask = Math.max(Math.abs(taskToGo[0] - tankPosX),Math.abs(taskToGo[1] - tankPosY));
        int distanceBetweenTaskAndFuelPump = Math.max(Math.abs(taskToGo[0] - nearestFuelpumpGoingFromTask[0]),Math.abs(taskToGo[1] - nearestFuelpumpGoingFromTask[1]));

        if(getFuelLevel() )
    }

    private Action loadWaste(){
        int[] positionToGo = seenTasks.get[getClosestIndex(seenTasks)];
        int positionToGoX = positionToGo[0];
        int positionToGoY = positionToGo[1];
        int wasteAmount = positionToGo[2];

        if(getCurrentCell(view) instanceof )
    }

//////////////////////////////////////////////
    private Action getFuel(Cell[][] view){
        if (getCurrentCell(view) instanceof FuelPump){
            //should never change the x and y values - included for peace of mind.
			tankerXPos = 0;
			tankerYPos = 0;
			return new RefuelAction();
        }
        else {
            return moveToPos(view, fuelStationPos);
        }
    }

    // private Action refuel(){
    //
    // }
    //
    //
    // private Action disposeWaste(){
    //
    // }
}
