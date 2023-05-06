package com.worldbuilder.mapgame;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Animal extends Lifeform {
    int propCounter = 1;
    Position targetSquare = null;

    public int getEyesight() {
        return eyesight;
    }

    public void setEyesight(int eyesight) {
        this.eyesight = eyesight;
    }

    private int eyesight = 40;
    private int speed;
    public double energy;

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    private String foodType = "Herbivore";

    public boolean isSwimmer() {
        return isSwimmer;
    }

    public void setSwimmer(boolean swimmer) {
        isSwimmer = swimmer;
    }

    private boolean isSwimmer = false;


    public Animal(String name, int speed, float camouflage, int lifespan, Position position, int propagationRate, int imgID, int habitat, int lifeFormID) {
        super(name, camouflage, lifespan, position, propagationRate * 4, imgID, habitat, lifeFormID);
        this.speed = speed;
        energy = 50;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void update(Tile[][] map, World world, Context context) {
        // Implement animal-specific behavior, like movement or hunting
        move(map);

        if (propCounter == MapUtils.resolution) {
            world.setDarwinPoints(world.darwinPoints +10);
            incrementAge();
            if (energy > 40) {
                procreate(map, world, context);
            }
            energy -= 2;

            // Check for nearby plants and consume them
            eat(map, world);

            // Check if energy has dropped below a threshold
            if (energy <= 0) {
                // If energy is too low, remove the animal from the world
                world.removeLifeform(this);
            }
            propCounter = 1;
        }

        propCounter++;
    }

    // Animal-specific methods, if any
    // ...

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void procreate(Tile[][] map, World world, Context context) {
        ArrayList<Animal> temp = new ArrayList<>(world.getAnimals());
        for (Animal animal : temp) {
            if (MapUtils.arePositionsClose(animal.getPosition(), position, 1)) {
                // Check if both animals are within 30 - 60% of their lifespan
                float agePercentageThisAnimal = (float) age / (float) lifespan;
                float agePercentageOtherAnimal = (float) animal.getAge() / (float) animal.getLifespan();

                if (agePercentageThisAnimal >= 0.3f && agePercentageThisAnimal <= 0.6f &&
                        agePercentageOtherAnimal >= 0.3f && agePercentageOtherAnimal <= 0.6f) {

                    Random random = new Random();
                    int rand = random.nextInt(100);
                    if (rand < propagationRate) {
                        // Procreation successful

                        Position newPos = MapUtils.findNewOffspringPosition(map, this);

                        if (newPos != null) {
                            Animal newAnimal = new Animal(name, speed, camouflage, lifespan, newPos, propagationRate, imgID, habitat, lifeFormID);
                            ImageView newAnimalImageView = new ImageView(context);
                            newAnimalImageView.setImageResource(newAnimal.imgID);
                            int xPosition = MapUtils.calculateXPosition(newPos.getX());
                            int yPosition = MapUtils.calculateYPosition(newPos.getY());

                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getImgSize(), getImgSize());
                            layoutParams.leftMargin = xPosition;
                            layoutParams.topMargin = yPosition;
                            newAnimalImageView.setLayoutParams(layoutParams);

                            newAnimal.setImageView(newAnimalImageView);
                            world.addLifeform(newAnimal);
                            world.getMapView().addView(newAnimalImageView); // Add the ImageView to the layout
                            map[newPos.getX()][newPos.getY()].setInHabitant(animal); //set the animal on the Tile in the map[][] array
                        }


                    }
                }
            }
        }
    }

    public void move(Tile[][] map) {
        if (targetSquare == null) {

            // Get the current position
            int x = position.getX();
            int y = position.getY();
            // Calculate the new position based on the animal's speed
            // and world conditions (e.g., terrain, obstacles)
            int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1}; // Possible x-axis movements
            int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1}; // Possible y-axis movements

            // Randomly select a direction to move
            int randomIndex = ThreadLocalRandom.current().nextInt(dx.length);
            int updatedX = x + dx[randomIndex] * (int) Math.round(speed);
            int updatedY = y + dy[randomIndex] * (int) Math.round(speed);


            List<Position> surroundings = MapUtils.generateSurroundingPositions(position, map, false, 1, eyesight);
            Position.sortByDistance(surroundings, position);
            Position targetedPos = null;


            for (Position position : surroundings) {
                Lifeform objectInQuestion = map[position.getX()][position.getY()].getInHabitant();
                //hungry. look for food
                if (energy < 40) {
                    if (foodType.equals("Herbivore")) {
                        if (objectInQuestion instanceof Plant) {
                            targetedPos = MapUtils.findPositionTowardsTarget(this.position, position, speed);
                        }
                    }
                    if (foodType.equals("Carnivore")) {
                        if (objectInQuestion instanceof Animal) {
                            targetedPos = MapUtils.findPositionTowardsTarget(this.position, position, speed);
                        }
                    }
                    if (foodType.equals("Omnivore")) {
                        if (objectInQuestion != null) {
                            targetedPos = MapUtils.findPositionTowardsTarget(this.position, position, speed);
                        }
                    }
                }

                if (objectInQuestion instanceof Animal && ((Animal) objectInQuestion).getFoodType().equals("Carnivore") && objectInQuestion.getLifeFormID() != lifeFormID) {
                    //run away from predator
                    targetedPos = MapUtils.findPositionAwayFromTarget(this.position, position, speed);
                    Log.d("runAwayPos", "Run Away Pos: X = " + targetedPos.getX() + "Y = " + targetedPos.getY());
                }
            }

            if (targetedPos != null) {
                updatedX = targetedPos.getX();
                updatedY = targetedPos.getY();
            }

            // Clamp the new position within the map boundaries
            updatedX = Math.max(0, Math.min(updatedX, map.length - 1));
            updatedY = Math.max(0, Math.min(updatedY, map[0].length - 1));

            // Check if the terrain at the new position is suitable for the animal
            Tile newTile = map[updatedX][updatedY];
            boolean canMoveToTile = false;

            //check that tile meets proper conditions
            if (isSwimmer) {
                // Animal can swim, so it can move on water tiles
                canMoveToTile = newTile.getTerrainType() == Tile.TerrainType.WATER;
            } else {
                // Animal can't swim, so it can move on all tiles except water
                canMoveToTile = newTile.getTerrainType() != Tile.TerrainType.WATER;
            }
            if (canMoveToTile) {
                targetSquare = new Position(updatedX, updatedY);
            }
        }

        if(targetSquare != null) {
            // Update the ImageView position
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

            int targetX = MapUtils.calculateXPosition(targetSquare.getX());
            int targetY = MapUtils.calculateYPosition(targetSquare.getY());
            int currentX = MapUtils.calculateXPosition(position.getX());
            int currentY = MapUtils.calculateYPosition(position.getY());

            int difY = targetY - currentY;
            int difX = targetX - currentX;

            int movementX = (difX / MapUtils.resolution) * propCounter;
            int movementY = (difY / MapUtils.resolution) * propCounter;

            int newX = MapUtils.calculateXPosition(position.getX()) + movementX;
            int newY = MapUtils.calculateYPosition(position.getY()) + movementY;

            layoutParams.leftMargin = newX;
            layoutParams.topMargin = newY;
            imageView.setLayoutParams(layoutParams);


            if (propCounter == MapUtils.resolution) {
                map[position.getX()][position.getY()].setInHabitant(null);
                // Update the animal's position
                position = targetSquare;

                map[position.getX()][position.getY()].setInHabitant(this);


                targetSquare = null;
            }
        }

    }

    public void eat(Tile[][] map, World world) {

        List<Lifeform> nearbyEdibles = new ArrayList<>();
        if (foodType.equals("Herbivore")) {
            nearbyEdibles.addAll(world.getNearbyPlants(position, 1)); // Adjust the value to control the search range
        }
        if (foodType.equals("Carnivore")) {
            List<Animal> animals = world.getNearbyAnimals(position, 1);
            for (Animal animal : animals) {
                if (animal.foodType.equals("Herbivore")) {
                    nearbyEdibles.add(animal);
                }
            }
        }
        if (foodType.equals("Omnivore")) {
            nearbyEdibles.addAll(world.getNearbyAnimals(position, 1));
            nearbyEdibles.addAll(world.getNearbyPlants(position, 1)); // Adjust the value to control the search range
        }
        for (Lifeform lifeform : nearbyEdibles) {
            if (energy == 100) {
                break;
            }
            // Consume the plant and increase the animal's energy
            energy += 10; // Adjust the value to control how much energy is gained from eating a plant
            world.removeLifeform(lifeform);
        }
    }


}