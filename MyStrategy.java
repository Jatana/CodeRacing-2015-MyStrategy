import static java.lang.StrictMath.*;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;

import model.Bonus;
import model.BonusType;
import model.Car;
import model.CarType;
import model.Game;
import model.Move;
import model.TileType;
import model.Unit;
import model.World;

public final class MyStrategy implements Strategy {

	int lastDbgX = -100;
	int lastDbgY = -100;
	char lastC = '0';
	ArrayList<CordPoint> carWay = new ArrayList<CordPoint>();

	CordPoint[][] expectedDirect;

	@Override
	public void move(Car self, World world, Game game, Move move) {
		if (!isInit) {
			init(self, world, game, move);
			isInit = true;
		}
		if (isMapDelaySpecify) {
			if (world.getTick() < mapSpecifyDelay) {
				return;
			} else {
				isMapDelaySpecify = false;
				wallCrashStart = world.getTick();
			}
		}

		int carX = (int) (self.getX() / game.getTrackTileSize());
		int carY = (int) (self.getY() / game.getTrackTileSize());
		CordPoint carPos = new CordPoint(carX, carY);
		if (!carPos.equals(carWay.get(carWay.size() - 1))) {
			carWay.add(carPos);
		}

		NextTurnTile t = getNextTurn(world, game, self);
		System.out.println(t.nextArea);
		System.out.println(t.speedStopCord);
		int[][] wayPoints = world.getWaypoints();
		int nextWaypointX = t.x;
		int nextWaypointY = t.y;
		CordPoint nextWaypoint = new CordPoint(t.x, t.y);
		double nextRealWaypointX;
		double nextRealWaypointY;
		if (t.nextArea == NextAreaType.DIAG_MOVES) {
			nextRealWaypointX = t.nextRealX;
			nextRealWaypointY = t.nextRealY;
		} else {
			nextRealWaypointX = (nextWaypointX + 0.5D)
					* game.getTrackTileSize();
			nextRealWaypointY = (nextWaypointY + 0.5D)
					* game.getTrackTileSize();
		}
		boolean haveBalanceTurn = true;
		double nextTurnRealX = nextRealWaypointX, nextTurnRealY = nextRealWaypointY;
		// System.out.printf("%d %d : %d %d\n", wayPoints[(curWaypointIndex + 1)
		// % wayPoints.length][0], wayPoints[(curWaypointIndex + 1)
		// % wayPoints.length][1], nextWaypointX, nextWaypointY);
		// System.out.printf("%d %d\n", carX, carY);
		int nextWaypointIndex = self.getNextWaypointIndex();
		// TileType curTile =
		// world.getTilesXY()[wayPoints[curWaypointIndex][0]][wayPoints[curWaypointIndex][1]];
		// TileType nextTile = world.getTilesXY()[nextWaypointX][nextWaypointY];
		// TODO Nagibe Shooter
		// System.out.println(self.getAngle());
		Car[] cars = world.getCars();
		{
			if (self.getProjectileCount() > 0) {
				for (Car car : cars) {
					if (!car.isFinishedTrack()
							&& !car.isTeammate()
							&& self.getDistanceTo(car) < game
									.getTrackTileSize() * 4
							&& car.getDurability() > 0) {
						// Point ProjectileVector = Geo
						// .toCord(self.getAngle(), 10);
						// Detail shootRay = new Detail(new Point(self.getX(),
						// self.getY()), new Point(self.getX()
						// + ProjectileVector.x, self.getY()
						// + ProjectileVector.y));
						// Detail targetRay = new Detail(car.getX(), car.getY(),
						// car.getX() + car.getSpeedX(), car.getY()
						// + car.getSpeedY());
						// double boost = 0.015;
						// double destroyerSpeed = game.getTireInitialSpeed();
						// if (Geo.isInterRays(shootRay, targetRay)) {
						// Point interPoint = Geo.getInterPoint(shootRay,
						// targetRay);
						// double targetDist = car.getDistanceTo(interPoint.x,
						// interPoint.y);
						// double destroyerDist = self.getDistanceTo(
						// interPoint.x, interPoint.y);
						// double targetSpeed = new Point(car.getSpeedX(),
						// car.getSpeedY()).distance;
						// double timeTargetToInter = (-(2 * targetSpeed) +
						// sqrt(((4 * targetSpeed) * (4 * targetSpeed))
						// + (8 * boost * targetDist)))
						// / (2 * boost);
						// timeTargetToInter = targetDist / targetSpeed;
						// double timeDestroyerToInter = destroyerDist
						// / destroyerSpeed;
						//
						// if (abs(timeTargetToInter - timeDestroyerToInter) <
						// 60
						// || (timeDestroyerToInter
						// - timeTargetToInter > 20 && timeDestroyerToInter
						// - timeTargetToInter < 200)) {
						// move.setThrowProjectile(true);
						// }
						// System.out.printf("%f %f -time\n",
						// timeDestroyerToInter, timeTargetToInter);
						// }
						// NEW TODO Focus Fire
						double x = min(abs(self.getAngle() - car.getAngle()),
								abs(PI - abs(self.getAngle() - car.getAngle())));
						// System.out.println(x);
						if ((abs(self.getAngleTo(car)) < ONE)
								&& ((car.getDistanceTo(
										car.getX() + car.getSpeedX(),
										car.getY() + car.getSpeedY()) < 5D) || (x < (PI / 30) || self
										.getDistanceTo(car) <= game
										.getTrackTileSize() * 0.75D))) {
							if (self.getType() == CarType.JEEP) {
								if (canShoot(self, car, game, world)) {
									move.setThrowProjectile(true);
								} else {
									System.out
											.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
								}
							} else {
								move.setThrowProjectile(true);
							}
						}

					}
				}
			}
		}
		for (Car car : cars) {
			if (!car.isTeammate()) {
				if (Geo.getVectorsAngle(
						new Point(self.getSpeedX(), self.getSpeedY()),
						new Point(car.getSpeedX(), car.getSpeedY())) < ONE * 2) {
					if (self.getDistanceTo(car) < game.getTrackTileSize() * 12) {
						if (abs(car.getAngleTo(self)) < ONE * 4) {
							if (self.getNextWaypointIndex() == car
									.getNextWaypointIndex()
									|| floorMod(
											self.getNextWaypointIndex() - 1,
											world.getWaypoints().length) == car
											.getNextWaypointIndex()) {
								move.setSpillOil(true);
							}
						}
					}
				}
			}
		}
		double maxPrefferedEnginePower = 1.0D;
		if (carsInTeam == 2 && world.getTick() < 240
				&& self.getType() == CarType.JEEP) {
			maxPrefferedEnginePower = 0.6D;
		}
		move.setEnginePower(maxPrefferedEnginePower);
		double speedForce = self.getDistanceTo(self.getX() + self.getSpeedX(),
				self.getY() + self.getSpeedY());
		if (isWallCrash) {
			if (world.getTick() - repairTimeStart > 110) {
				isWallCrash = false;
				levelBackward = min(levelBackward + 10, maxLevelBackward);
				wallCrashStart = world.getTick();
				startPlavHod = world.getTick();
			} else {
				move.setEnginePower(-1.0D);
				double turnAngle = self.getAngleTo(nextRealWaypointX,
						nextRealWaypointY);
				move.setWheelTurn(-turnAngle * 3);
				System.out.println(t.x + " " + t.y + "chtot");
			}
			return;
		} else if (speedForce < 1D && world.getTick() > 300
				&& self.getDurability() > 0) {
			if (world.getTick() - wallCrashStart > 80) {
				isWallCrash = true;
				repairTimeStart = world.getTick();
			}
		} else {
			if (world.getTick() - startPlavHod > 150) {
				levelBackward = minLevelBackward;
			}

			wallCrashStart = world.getTick();
		}

		if (lastDbgX == t.x && lastDbgY == t.y) {

		} else {
			System.out.printf("%d %d ! %d %d ! %d %d ", t.x, t.y,
					nextWaypointX, nextWaypointY,
					(int) (self.getX() / game.getTrackTileSize()),
					(int) (self.getY() / game.getTrackTileSize()));
			System.out.println(t.type);
			lastDbgX = t.x;
			lastDbgY = t.y;
		}
		// System.out.print(" ");
		// System.out.println(nextTile);

		// if (isLineTile(curTile) && curTile != lastLineTile) {
		// lastLineTile = curTile;
		// System.out.print(curTile);
		// System.out.print(' ');
		// System.out.println(nextTile);
		// } else if (isTurnTile(curTile)) {
		// if (lastLineTile == TileType.VERTICAL) {
		// lastLineTile = TileType.HORIZONTAL;
		// } else {
		// lastLineTile = TileType.VERTICAL;
		// }
		// }

		// if (self.getDistanceTo(nextWaypointX, nextWaypointY) > 2000) {
		// move.setEnginePower(1.0D);
		// } else {
		// move.setBrake(true);
		// move.setEnginePower(0.50D);
		// }
		char c = t.speedStopCord;
		if (c != lastC) {
			System.out.printf("%c ", c);
			System.out.println(world.getTilesXY()[(int) ((self.getX()) / game
					.getTrackTileSize())][(int) ((self.getY()) / game
					.getTrackTileSize())]);
			System.out.printf("%f %f\n", self.getSpeedX(), self.getSpeedY());
			lastC = c;
		}
		// System.out.printf("%f %f\n", self.getSpeedX(), self.getSpeedY());
		double cornerTileOffset = 1D * game.getTrackTileSize();
		double balanceDrift = 0.27D * game.getTrackTileSize();
		double anglePlus = 1;
		if (cnt < 1) {
			System.out.println("Strategy Name: Super Strategy 777");
			System.out.println(world.getMapName());
			// System.out.println(curTile);
			System.out.println("NEWWWWW");
			System.out.printf("%d %d \n", self.getNextWaypointX(),
					self.getNextWaypointY());
			for (int i = 0; i < wayPoints.length; i++) {
				System.out.printf("%d %d %s\n", wayPoints[i][0],
						wayPoints[i][1],
						world.getTilesXY()[wayPoints[i][0]][wayPoints[i][1]]);
			}
			cnt++;
		}
		// System.out.printf("%f %f : %f %f\n", self.getX(), self.getY(),
		// nextRealWaypointX, nextRealWaypointY);
		// System.out.println(nextTurn);

		// System.out.printf("%d %d\n", nextWaypointX, nextWaypointY);
		// System.out.printf("%d %d\n", wayPoints[curWaypointIndex][0],
		// wayPoints[curWaypointIndex][1]);
		double timeRun = (speedForce - 3) / brakeStopFactor;
		double DistToStop = (((speedForce - 3) / 2) * timeRun) + (3 * timeRun);
		// System.out.println(timeRun);
		// System.out.println(speedForce);
		double speedDivFactor = 17;
		double speedCorrectionFactor = 1;
		if (speedForce <= 9.2D) {
			cornerTileOffset *= 0.5D;
		} else if (speedForce <= 20) {
			speedDivFactor = 12;
		} else if (speedForce <= 26D) {
			speedDivFactor = 13.5D;
		}
		// speedDivFactor = speedForce - 13;
		// } else if (speedForce >= 25) {
		// speedCorrectionFactor = 1;
		// cornerTileOffset *= 2;
		// } else {
		// speedCorrectionFactor = 1.2D;
		// }
		double limitForceSpeedStop = 19.0D;
		double limitSpeedBrake = 23D;
		double limitDriftSpeed = 20D;
		if (t.nextArea == NextAreaType.PREPARE_TO_SMALL_TURN) {

			// limitSpeedBrake = 25D;
			// limitDriftSpeed = 23D;
			// speedCorrectionFactor = 1D;
//			if (speedForce <= 15) {
////				cornerTileOffset *= 0.4D;
//				speedCorrectionFactor = 0.8D;
//			} else if (speedForce <= 20) {
//				speedCorrectionFactor = 1D;
//			}
			if (speedForce >= 15) {
				speedCorrectionFactor = 1.1D;
			}
		} else if (t.nextArea == NextAreaType.PREPARE_TO_REVERSE) {
			speedCorrectionFactor = 0.3D;
		}

		if (carX == 2 && carY == 3) {
			// System.out.println(speedForce);
		}
		Point driftPoint = new Point(nextRealWaypointX
				+ (balanceDrift * t.factor.x * -1), nextRealWaypointY
				+ (balanceDrift * t.factor.y * -1));
		double driftError = -1;
		if (t.speedStopCord == 'X') {
			driftError = abs(driftPoint.x - self.getX());
		} else if (t.speedStopCord == 'Y') {
			driftError = abs(driftPoint.y - self.getY());
		}
		System.out.println(driftError);
		boolean riskDolb = false;
		Detail[] circlePoints = getQuadrateDetails(carPos, game);
		for (int i = 0; i < circlePoints.length; i++) {
			Point circleCenter = circlePoints[i].point1.getClone();
			if (self.getDistanceTo(circleCenter.x, circleCenter.y) < game
					.getTrackTileSize() * 0.6D
					&& abs(self.getAngleTo(circleCenter.x, circleCenter.y)) < PI / 14) {
				System.out.println("WARNINGG!!!!!!WARNING!!!!!");
				// move.setEnginePower(0.2D);
				// move.setWheelTurn(300000);
				// TODO Balance break
				// if (speedForce > 10D) {
				// move.setBrake(true);
				// }
				riskDolb = true;
				break;
			}

		}

		// TODO
		if ((t.nextArea != NextAreaType.DIAG_MOVES)
				&& (t.nextArea != NextAreaType.PREPARE_TO_DIAG
						&& (t.nextArea != NextAreaType.END_DIAG_TURN)
						&& (t.nextArea != NextAreaType.END_DIAG_SHARP_TURN) && (t.nextArea != NextAreaType.SHARP_TURN_TO_DIAG))) {
			System.out.println(t.x + " " + t.y + " <--" + " " + carPos);
			// Detail carVectorDrift = new Detail(new Point(self.getX()
			// + (self.getSpeedX() * 20), self.getY()
			// + (self.getSpeedY() * 20)), new Point(
			// (t.next2Tile.x + 0.5D) * game.getTrackTileSize(),
			// (t.next2Tile.y + 0.5D) * game.getTrackTileSize()));
			// Detail[] collisionDetails = getQuadrateDetails(t.collisionTile,
			// game);
			boolean isSafeTurn = (self.getDistanceTo(nextTurnRealX,
					nextTurnRealY) < game.getTrackTileSize() * 0.7)
					|| (driftError <= 100D);
			if (!riskDolb && self.getDistanceTo(nextTurnRealX, nextTurnRealY) < (speedForce / speedDivFactor)
					* game.getTrackTileSize() * speedCorrectionFactor) {
				// System.out.printf("This ebnt factor offset = %s\n",
				// t.factor.toString());
				double speedToStop;
				if (t.speedStopCord == 'X') {
					speedToStop = self.getSpeedY();
				} else {
					speedToStop = self.getSpeedX();
				}
				if (speedToStop > limitForceSpeedStop) {
					// move.setBrake(true);
					anglePlus = 179;
				}
				if (self.getDistanceTo(self.getX() + self.getSpeedX(),
						self.getY() + self.getSpeedY()) > limitSpeedBrake) {
					move.setBrake(true);
					anglePlus = 179;
				}

				// if (abs(self.getAngleTo(nextRealWaypointX,
				// nextRealWaypointY)) > PI / 3.6D) {
				// anglePlus = 179;
				// if (speedForce >= 15D) {
				// move.setBrake(true);
				// }
				// }

				if (t.nextArea == NextAreaType.PREPARE_TO_SMALL_TURN) {
					// move.setEnginePower(0.5D);
				} else if (t.nextArea == NextAreaType.SHARP_TURN) {
					move.setEnginePower(0.5D);
				}
				nextRealWaypointX += cornerTileOffset * t.factor.x;
				nextRealWaypointY += cornerTileOffset * t.factor.y;

			} else {
				// System.out.println(lastLineTile);
				double carDriftReserveX = self.getX() % game.getTrackTileSize();
				if (self.getSpeedX() >= 0) {
					carDriftReserveX = game.getTrackTileSize()
							- carDriftReserveX;
				}
				double carDriftReserveY = self.getY() % game.getTrackTileSize();
				if (self.getSpeedY() >= 0) {
					carDriftReserveY = game.getTrackTileSize()
							- carDriftReserveY;
				}

				double ignoreDriftSpeed = 2D;
				if (t.speedStopCord == 'X'
						&& Math.abs(self.getSpeedX()) > limitDriftSpeed
						&& false) {

					double timeDrift = Math.abs(self.getSpeedX()
							- ignoreDriftSpeed)
							/ slowdownFactor;
					timeDrift = Math.min(80, timeDrift);
					double lengthDrift = Math.abs(self.getSpeedX() / 2)
							* timeDrift;
					if (lengthDrift > carDriftReserveX) {
						nextRealWaypointX += balanceDrift
								* getZnak(self.getSpeedX()) * -1;
					}
					if (Math.abs(self.getSpeedX()) > 20.0D) {
						// move.setWheelTurn(1.0D);

						// move.setBrake(true);
					}
					// if (self.getSpeedX() > 13D
					// && carDriftReserveX < game.getTrackTileSize() * 0.4) {
					// move.setBrake(true);
					// }
					// System.out.printf("%f: %f -> ", self.getSpeedX(),
					// nextRealWaypointX);
					// nextRealWaypointX += balanceDrift
					// * getZnak(self.getSpeedX()) * -1;
					// System.out.printf("%f\n", nextRealWaypointX);
					anglePlus = 2;
				} else if (t.speedStopCord == 'Y'
						&& Math.abs(self.getSpeedY()) > limitDriftSpeed
						&& false) {

					double timeDrift = Math.abs(self.getSpeedY()
							- ignoreDriftSpeed)
							/ slowdownFactor;
					timeDrift = Math.min(80, timeDrift);
					double lengthDrift = Math.abs(self.getSpeedY() / 2)
							* timeDrift;
					if (lengthDrift > carDriftReserveY) {
						nextRealWaypointY += balanceDrift
								* getZnak(self.getSpeedY()) * -1;
					}
					if (Math.abs(self.getSpeedY()) > 20.0D) {
						// move.setBrake(true);
					}
					// if (self.getSpeedY() > 13D
					// && carDriftReserveY < game.getTrackTileSize() * 0.4) {
					// move.setBrake(true);
					// }
					// nextRealWaypointY += balanceDrift
					// * getZnak(self.getSpeedY()) * -1;
					anglePlus = 2;
				} else {

					// if (t.speedStopCord == 'X'
					// && Math.abs(self.getSpeedX()) >= 3.0D) {
					// move.setWheelTurn(0.2D * getZnak(self.getSpeedY()));
					// } else if (t.speedStopCord == 'Y'
					// && Math.abs(self.getSpeedY()) >= 3.0D) {
					// move.setWheelTurn(0.2D * getZnak(self.getSpeedX()));
					// } else
					// TODO Bonues Collecter

					// System.out.printf("%s : %s\n", bounds[0].toString(),
					// bounds[1].toString());
					double factor = -1;
					if (t.assumeState == NextAssumeState.POST_DIAG) {
						if (speedForce >= 15D) {
							move.setBrake(true);
						}
						anglePlus = 200;
						System.out.println("FORCES");
					} else if (t.nextArea == NextAreaType.LONG_TURN) {
						factor = 0;
						if (abs(self.getAngleTo(nextRealWaypointX,
								nextRealWaypointY)) > PI / 3
								&& (speedForce <= 10.0D)) {

						} else {
							nextRealWaypointX += cornerTileOffset * t.factor.x
									* 0.35D;
							nextRealWaypointY += cornerTileOffset * t.factor.y
									* 0.35D;
						}

						// System.out.println("configured");
						anglePlus = 10;
					} else {
						Point[] bounds = Geo.getLeftRightRectBounds(carPos,
								nextWaypoint, game);
						Bonus[] bonuses = world.getBonuses();
						Bonus bestBonus = null;
						for (Bonus bonus : bonuses) {
							if (bonus.getType() == BonusType.NITRO_BOOST
									|| (self.getDurability() < 0.65D && bonus
											.getType() == BonusType.REPAIR_KIT)
									|| (bonus.getType() == BonusType.PURE_SCORE && abs(self
											.getAngleTo(bonus)) < PI / 18)) {
								if (bonus.getDistanceTo(nextRealWaypointX,
										nextRealWaypointY) > game
										.getTrackTileSize() * 2.4) {
									if (Geo.isInRect(
											bounds[0],
											bounds[1],
											new Point(bonus.getX(), bonus
													.getY()))
											&& abs(self.getAngleTo(bonus)) < PI / 6) {
										// System.out.println(bounds[0].toString()
										// + bounds[1].toString()
										// + new Point(bonus.getX(), bonus
										// .getY()));
										if (bestBonus == null
												|| self.getDistanceTo(bestBonus) > self
														.getDistanceTo(bonus)) {
											if (abs(abs(self.getAngleTo(
													nextRealWaypointX,
													nextRealWaypointY))
													+ abs(self
															.getAngleTo(bonus))) < PI / 12) {
												bestBonus = bonus;
											}
										}
									}

								}
							}
						}
						if (bestBonus == null) {
							nextRealWaypointX += balanceDrift * t.factor.x
									* factor;
							nextRealWaypointY += balanceDrift * t.factor.y
									* factor;
						} else {
							// System.out.println(bestBonus);
							if ((self.getAngleTo(bestBonus) > PI / 50) || true) {
								nextRealWaypointX = bestBonus.getX();
								nextRealWaypointY = bestBonus.getY();
							} else {
								return;
							}
						}
						anglePlus = 10;
					}
				}
				if (self.getDistanceTo(nextTurnRealX, nextTurnRealY) > game
						.getTrackTileSize() * 4
						&& abs(self.getAngleTo(nextRealWaypointX,
								nextRealWaypointY)) < ONE * 3) {
					if (world.getTick() > game.getInitialFreezeDurationTicks() * 2.4D) {
						move.setUseNitro(true);
					}
				}
				move.setEnginePower(maxPrefferedEnginePower);
			}

			if (self.getDistanceTo(nextRealWaypointX, nextRealWaypointY) > game
					.getTrackTileSize() * 4
					&& abs(self
							.getAngleTo(nextRealWaypointX, nextRealWaypointY)) < ONE * 3) {
				if (world.getTick() > game.getInitialFreezeDurationTicks() * 2.4D) {
					move.setUseNitro(true);
				}
			}
		} else {
			if (self.getDistanceTo(nextRealWaypointX, nextRealWaypointY) > game
					.getTrackTileSize() * 4
					&& abs(self
							.getAngleTo(nextRealWaypointX, nextRealWaypointY)) < ONE * 3) {
				if (world.getTick() > game.getInitialFreezeDurationTicks() * 2.4D) {
					move.setUseNitro(true);
				}
			}
			if (t.nextArea == NextAreaType.PREPARE_TO_DIAG
					|| t.nextArea == NextAreaType.SHARP_TURN_TO_DIAG) {
				// if (speedForce >= 35D) {
				// speedDivFactor = 20;
				// } else if (speedForce > 25D) {
				// speedDivFactor = 25;
				// } else {
				// speedDivFactor = 17;
				// }
				if (t.nextArea == NextAreaType.SHARP_TURN_TO_DIAG) {
					// nextRealWaypointX += cornerTileOffset * t.factor.x * 1D;
					// nextRealWaypointY += cornerTileOffset * t.factor.y * 1D;
					if (speedForce >= 10D) {
						move.setBrake(true);
					}
				}
				// if (abs(self.getAngleTo(nextRealWaypointX,
				// nextRealWaypointY)) > PI / 6) {
				// anglePlus = 20;
				// move.setEnginePower(0.2D);
				// System.out.println("Stop Kran");
				// }
				Point next2RealPoint = getRealCenterCordPoint(t.next2Point,
						game);
				nextRealWaypointX = next2RealPoint.x;
				nextRealWaypointY = next2RealPoint.y;
				Point next3Point = getRealCenterCordPoint(t.next3Point, game);
				Detail[] collisionDetails = getQuadrateDetails(t.collisionTile,
						game);
				Detail assumeWayVector = new Detail(new Point(self.getX(),
						self.getY()),
						next3Point.getMiddle(getRealCenterCordPoint(
								t.next4Point, game)));
				boolean isSafeWay = !(Geo.isInterDetails(collisionDetails,
						new Detail[] { assumeWayVector }));
				boolean needToTurn = false;
				double lenWayGo = self.getDistanceTo(nextRealWaypointX,
						nextRealWaypointY) - (game.getTrackTileSize() * 0.5D);
				double timeGo = speedForce / 1;
				double distGo = speedForce * timeGo;

				if (lenWayGo < 0) {
					// needToTurn = true;
				} else {
					if (self.getDistanceTo(nextRealWaypointX, nextRealWaypointY) < (game
							.getTrackTileSize() * 0.8D) + distGo) {
						needToTurn = true;
					}
				}
				// self.getDistanceTo(nextRealWaypointX, nextRealWaypointY) <
				// (speedForce / speedDivFactor)
				// + game.getTrackTileSize() * 1
				// System.out.println(isSafeWay);
				if (needToTurn || isSafeWay) {
					if (speedForce > 20D
							&& self.getDistanceTo(nextRealWaypointX,
									nextRealWaypointY) < (speedForce / speedDivFactor)
									* game.getTrackTileSize() * 0.6) {
						anglePlus = 10;
						// move.setBrake(true);
					}
					// TODO
					if (speedForce >= 20.0D) {
						move.setBrake(true);
					}
					// System.out.println("WHYYY");
					// nextRealWaypointX = t.nextRealX;
					// nextRealWaypointY = t.nextRealY;
					cornerTileOffset = game.getTrackTileSize() * 0.4D;
					nextRealWaypointX += cornerTileOffset * t.factor.x;
					nextRealWaypointY += cornerTileOffset * t.factor.y;
					/*
					 * System.out.printf("%f %f %f %f <- cords\n", t.nextRealX,
					 * t.nextRealY, nextRealWaypointX, nextRealWaypointY);
					 * System.out .println(carPos.toString() + new
					 * CordPoint(t.x, t.y) + t.next2Point.toString());
					 * System.out.println(t.factor.x + " " + t.factor.y);
					 * System.out.println(distGo);
					 */
				} else {
					// if (self.getDistanceTo(nextRealWaypointX,
					// nextRealWaypointY) < (speedForce / speedDivFactor)
					// * game.getTrackTileSize() * 2) {
					// move.setEnginePower(0.5D);
					// }
					if (t.assumeState == NextAssumeState.POST_DIAG) {
						if (speedForce >= 15D) {
							move.setBrake(true);
						}
						anglePlus = 200;
						System.out.println("FORCES");
					} else {
						nextRealWaypointX += balanceDrift * t.factor.x * -1;
						nextRealWaypointY += balanceDrift * t.factor.y * -1;
					}
				}
			} else if (t.nextArea == NextAreaType.END_DIAG_TURN
					|| t.nextArea == NextAreaType.END_DIAG_SHARP_TURN) {

				double downCorrection = game.getTrackTileSize() * 0.3D;
				Point factorDown = new Point(
						t.next2Point.substract(t.next3Point));
				Point middleNextPoint = getRealCenterCordPoint(carPos, game)
						.getMiddle(getRealCenterCordPoint(t.next2Point, game));
				middleNextPoint = middleNextPoint.getSubstract(new Point(
						downCorrection * factorDown.x, downCorrection
								* factorDown.y));
				System.out.println(factorDown + " _ << << <<");
				System.out.println(middleNextPoint.toString()
						+ new Point(t.nextRealX, t.nextRealY).toString());
				speedDivFactor = 17;
				if (t.nextArea == NextAreaType.END_DIAG_SHARP_TURN) {
					if (speedForce > 19D) {
						// move.setEnginePower(0.6D);
						// move.setBrake(true);
						// nextRealWaypointX = t.next2Turn.x;
						// nextRealWaypointY = t.next2Turn.y;
					}

				}

				System.out.printf("HERRRRRS -> %f \n", speedForce);
				// speedDivFactor += 5;
				if (self.getDistanceTo(nextTurnRealX, nextTurnRealY) < (speedForce / speedDivFactor)
						* game.getTrackTileSize()
						&& speedForce >= 10) {
					boolean forceTurn = false;
					if (t.nextArea == NextAreaType.END_DIAG_SHARP_TURN) {
						if (speedForce > 10D
								&& self.getDistanceTo(nextTurnRealX,
										nextTurnRealY) <= game
										.getTrackTileSize() * 0.4D) {
							forceTurn = true;
							move.setEnginePower(0.1D);
						}
						// System.out.println("Very SHarp!!!");
					}

					System.out.print(t.next2Turn);
					// System.out.printf(" %f %f\n", t.nextRealX, t.nextRealY);
					anglePlus = 10;
					if ((speedForce >= 8D && (self.getDistanceTo(nextTurnRealX,
							nextTurnRealY) < game.getTrackTileSize() * 0.3D))
							|| forceTurn) {
						nextRealWaypointX = t.next2Turn.x;
						nextRealWaypointY = t.next2Turn.y;
						System.out.println("TTTTTTTTTTTTTTT");
					} else {
						if (riskDolb) {
							anglePlus = 100;
						}
						nextRealWaypointX = t.nextRealX;
						nextRealWaypointY = t.nextRealY;
						// nextRealWaypointX = middleNextPoint.x;
						// nextRealWaypointY = middleNextPoint.y;
					}
				} else {
					nextRealWaypointX = t.nextRealX;
					nextRealWaypointY = t.nextRealY;
					// nextRealWaypointX = middleNextPoint.x;
					// nextRealWaypointY = middleNextPoint.y;
				}
				// move.setBrake(false);
				// if (speedForce > 10D) {
				// move.setBrake(true);
				// }
				// nextRealWaypointX = t.nextRealX;
				// nextRealWaypointY = t.nextRealY;
			} else {
				anglePlus = 3.4;
			}

		}
		double angleToWaypoint = self.getAngleTo(nextRealWaypointX,
				nextRealWaypointY);
		// TileType thisTile =
		// prevTurn[self.getNextWaypointX()][self.getNextWaypointY()];
		// if (isTurnTile(curTile)) {
		// if (self.getDistanceTo(self.getX() + self.getSpeedX(), self.getY()
		// + self.getSpeedY()) > 15) {
		// move.setBrake(true);
		// }
		//
		// move.setEnginePower(1.0D);
		// // System.out.printf("%f %f \n", self.getSpeedX(),
		// // self.getSpeedY());
		// angleToWaypoint *= 32;
		// // System.out.println("It works");
		// // }
		if (riskDolb) {
			// move.setEnginePower(-1.0D);
			// anglePlus = -30;
			// if (speedForce >= 9D) {
			// move.setBrake(true);
			// }
			System.out.println("RISKKKK DOLB");
		}

		move.setWheelTurn(angleToWaypoint * anglePlus);

		// System.out.printf("%f \n", angleToWaypoint * 4);
		// move.setEnginePower(0.3D);
		// System.out.printf("%f %f <- \n", self.getSpeedX(), self.getSpeedY());
	}

	double getZnak(double x) {
		return x / Math.abs(x);
	}

	private class DElem {
		int x;
		int y;
		int direct;
		int wayWeight;
		int prio;

		DElem(int x, int y, int direct, int wayWeight) {
			this.x = x;
			this.y = y;
			this.direct = direct;
			this.wayWeight = wayWeight;
			this.prio = wayWeight;
		}

		DElem() {
			// for supermans
		}
	}

	private class DElemComparator implements Comparator<DElem> {
		@Override
		public int compare(DElem a, DElem b) {
			if (a.prio < b.prio) {
				return -1;
			}
			if (a.prio > a.prio) {
				return 1;
			}
			return 0;
		}
	}

	private static class TileMove {
		static int[][] MOVES = { { 0, 1 }, { 1, 0 }, { -1, 0 }, { 0, -1 } };
		static int[][] VERTICAL_MOVES = { { 0, 1 }, { 0, -1 } };
		static int[][] HORIZONTAL_MOVES = { { 1, 0 }, { -1, 0 } };
		static int[][] LEFT_TOP_CORNER_MOVES = { { 1, 0 }, { 0, 1 } };
		static int[][] LEFT_BOTTOM_CORNER_MOVES = { { 1, 0 }, { 0, -1 } };
		static int[][] RIGHT_TOP_CORNER_MOVES = { { -1, 0 }, { 0, 1 } };
		static int[][] RIGHT_BOTTOM_CORNER_MOVES = { { -1, 0 }, { 0, -1 } };
		static int[][] RIGHT_HEADED_T_MOVES = { { 0, 1 }, { 0, -1 }, { 1, 0 } };
		static int[][] TOP_HEADED_T_MOVES = { { 1, 0 }, { -1, 0 }, { 0, -1 } };
		static int[][] BOTTOM_HEADED_T_MOVES = { { 1, 0 }, { -1, 0 }, { 0, 1 } };
		static int[][] LEFT_HEADED_T_MOVES = { { 0, 1 }, { 0, -1 }, { -1, 0 } };

		static int[][] getMoves(TileType t) {
			switch (t) {
			case VERTICAL:
				return VERTICAL_MOVES;
			case HORIZONTAL:
				return HORIZONTAL_MOVES;
			case LEFT_TOP_CORNER:
				return LEFT_TOP_CORNER_MOVES;
			case RIGHT_BOTTOM_CORNER:
				return RIGHT_BOTTOM_CORNER_MOVES;
			case LEFT_BOTTOM_CORNER:
				return LEFT_BOTTOM_CORNER_MOVES;
			case RIGHT_HEADED_T:
				return RIGHT_HEADED_T_MOVES;
			case TOP_HEADED_T:
				return TOP_HEADED_T_MOVES;
			case BOTTOM_HEADED_T:
				return BOTTOM_HEADED_T_MOVES;
			case RIGHT_TOP_CORNER:
				return RIGHT_TOP_CORNER_MOVES;
			case LEFT_HEADED_T:
				return LEFT_HEADED_T_MOVES;
			default:
				return MOVES;
			}
		}
	}

	class TileState {
		// TODO kak bbI
		boolean hasDiagEdge;
		boolean hasNormEdge;
		int weightDiag = -1;
		int weightNorm = -1;
		CordPoint prevDiag1, prevDiag2;

		TileState() {
			hasDiagEdge = false;
			hasNormEdge = false;
		}
	}

	class MoveProperty {
		int weight;
		int newPtr;

		MoveProperty(int weight, int newPtr) {
			this.weight = weight;
			this.newPtr = newPtr;
		}
	}

	class PtrInfo {
		private final int[][] cords = { { 0, -1 }, { -1, 0 }, { 0, 1 },
				{ 1, 0 } };
		MoveProperty[][] moves;

		PtrInfo(MoveProperty[][] properties) {
			moves = properties.clone();
		}

		MoveProperty[] getEdgeInfo(CordPoint d) {
			for (int i = 0; i > cords.length; i++) {
				if (cords[i][0] == d.x && cords[i][1] == d.y) {
					return moves[i];
				}
			}
			return null;
		}
	}

	MoveProperty[][][] MoveProperties = new MoveProperty[][][] {
			// 0
			{ { new MoveProperty(1, 0) },
					{ /* new MoveProperty(2, 3), */new MoveProperty(1, 1) },
					{ new MoveProperty(6, 6) },
					{ /* new MoveProperty(2, 9), */new MoveProperty(1, 11) } },
			// 1
			{ { new MoveProperty(1, 0), new MoveProperty(1, 2) },
					{ new MoveProperty(2, 3) }, { new MoveProperty(4, 6) },
					{ new MoveProperty(2, 9) } },
			// 2
			{ { new MoveProperty(2, 0) },
					{ new MoveProperty(1, 3), new MoveProperty(1, 1) },
					{ new MoveProperty(2, 6) }, { new MoveProperty(4, 9) } },
			// 3
			{ { /* new MoveProperty(2, 0) , */new MoveProperty(1, 2) },
					{ new MoveProperty(1, 3) },
					{ /* new MoveProperty(2, 6), */new MoveProperty(1, 4) },
					{ new MoveProperty(6, 9) } },
			// 4
			{ { new MoveProperty(3, 0) },
					{ new MoveProperty(1, 3), new MoveProperty(1, 5) },
					{ new MoveProperty(2, 6) }, { new MoveProperty(4, 9) } },
			// 5
			{ { new MoveProperty(4, 0) }, { new MoveProperty(2, 3) },
					{ new MoveProperty(1, 6), new MoveProperty(1, 6) },
					{ new MoveProperty(3, 9) } },
			// 6
			{ { new MoveProperty(6, 0) },
					{ /* new MoveProperty(2, 3), */new MoveProperty(1, 5) },
					{ new MoveProperty(1, 6) },
					{ /* new MoveProperty(2, 9), */new MoveProperty(1, 7) } },
			// 7
			{ { new MoveProperty(3, 0) }, { new MoveProperty(2, 3) },
					{ new MoveProperty(1, 6), new MoveProperty(1, 8) },
					{ new MoveProperty(2, 9) } },
			// 8
			{ { new MoveProperty(2, 0) }, { new MoveProperty(3, 3) },
					{ new MoveProperty(2, 6) },
					{ new MoveProperty(1, 9), new MoveProperty(1, 7) } },
			// 9
			{ { /* new MoveProperty(2, 0), */new MoveProperty(1, 10) },
					{ new MoveProperty(6, 3) },
					{ /* new MoveProperty(2, 6), */new MoveProperty(1, 8) },
					{ new MoveProperty(1, 9) } },
			// 10
			{ { new MoveProperty(2, 0) }, { new MoveProperty(3, 3) },
					{ new MoveProperty(2, 6) },
					{ new MoveProperty(1, 9), new MoveProperty(1, 11) } },
			// 11
			{ { new MoveProperty(1, 0), new MoveProperty(1, 10) },
					{ new MoveProperty(2, 3) }, { new MoveProperty(3, 6) },
					{ new MoveProperty(2, 9) } }, };

	int getMoveId(int dx, int dy) {
		assert dx == 0 || dy == 0;
		if (dx == 0) {
			if (dy == -1) {
				return 0;
			} else {
				return 2;
			}
		} else {
			if (dx == -1) {
				return 1;
			} else {
				return 3;
			}
		}
	}

	int getDirectId(CordPoint p1, CordPoint p2) {
		CordPoint p = p2.substract(p1);
		return getMoveId(p.x, p.y) * 3;
	}

	int dbgCnt1 = 0;

	// TODO Think Prev
	int bfs(TileType[][] tiles, CordPoint fromPoint, CordPoint toPoint,
			int fromDirection, ArrayList<CordPoint> fullWay, World world,
			CordPoint prevFirst) {
		// if (tiles[toPoint.x][toPoint.y] == TileType.UNKNOWN
		// || tiles[fromPoint.x][fromPoint.y] == TileType.UNKNOWN) {
		// return -1;
		// }
		int cords[][];
		int[][][] dist = new int[tiles.length][][];
		boolean[][] visited = new boolean[tiles.length][];
		int[][][][] prev = new int[tiles.length][][][];
		for (int i = 0; i < tiles.length; i++) {
			dist[i] = new int[tiles[i].length][];
			visited[i] = new boolean[tiles[i].length];
			prev[i] = new int[tiles[i].length][12][3];
			for (int j = 0; j < tiles[i].length; j++) {
				dist[i][j] = new int[12];
				for (int k = 0; k < dist[i][j].length; k++) {
					dist[i][j][k] = -1;
					prev[i][j][k][0] = -1;
					prev[i][j][k][1] = -1;
					prev[i][j][k][2] = -1;
					// INIT
				}
				visited[i][j] = false;
			}
		}
		PriorityQueue<DElem> d = new PriorityQueue<DElem>(new DElemComparator());
		DElem dFirst = new DElem(fromPoint.x, fromPoint.y, fromDirection, 0);
		d.add(dFirst);
		dist[fromPoint.x][fromPoint.y][fromDirection] = 0;
		if (prevFirst != null) {
			for (int i = 0; i < 12; i++) {
				prev[fromPoint.x][fromPoint.y][i] = new int[] { prevFirst.x,
						prevFirst.y, /* TODO ->> */0 };
			}
		}
		while (!d.isEmpty()) {
			DElem curDElem = d.remove();
			int curX = curDElem.x;
			int curY = curDElem.y;
			// System.out.println(new CordPoint(curX, curY));
			int curDirect = curDElem.direct;
			int weightToCur = curDElem.wayWeight;
			// if (dist[curX][curY][curDirect] != -1 &&
			// dist[curX][curY][curDirect] < weightToCur) {
			// continue;
			// }
			// dist[curX][curY][curDirect] = weightToCur;
			cords = TileMove.getMoves(tiles[curX][curY]);
			for (int i = 0; i < cords.length; i++) {
				int dx = cords[i][0];
				int dy = cords[i][1];
				int subX = curX + dx;
				int subY = curY + dy;
				// map specifying check
				boolean isMagicEdge = true;
				if (world.getMapName().equals("map06")) {
					if ((curX == 3 && curY == 14) && (subX == 3 && subY == 13)) {
						isMagicEdge = false;
					} else if ((curX == 9 && curY == 14)
							&& (subX == 9 && subY == 13)) {
						isMagicEdge = false;
					}
				} else if (world.getMapName().equals("_ud1")) {
					if ((curX == 10 && curY == 15)
							&& (subX == 10 && curY == 14)) {
						isMagicEdge = false;
					} else if (curX == 4 && curY == 1 && subX == 5 && subY == 1) {
						isMagicEdge = false;
					} else if (curX == 6 && curY == 0 && subX == 6 && subY == 1) {
						isMagicEdge = false;
					} else if (curX == 11 && curY == 5 && subX == 11
							&& subY == 6) {
						isMagicEdge = false;
					} else if (curX == 15 && curY == 10 && subX == 14
							&& subY == 10) {
						isMagicEdge = false;
					} else if (curX == 4 && curY == 3 && subX == 4 && subY == 4) {
						isMagicEdge = false;
					}
				} else if (world.getMapName().equals("_tyamgin")) {
					int[][] notMagicEdges = { {2, 0, 1, 0}, { 13, 2, 13, 3 }, {4, 5, 4, 4}, {10, 5, 10, 4}, {8, 2, 8, 3}, {6, 2, 6, 1} };
					for (int j = 0; j < notMagicEdges.length; j++) {
						if (curX == notMagicEdges[j][0]
								&& curY == notMagicEdges[j][1]
								&& subX == notMagicEdges[j][2]
								&& subY == notMagicEdges[j][3]) {
							isMagicEdge = false;
						}
					}
				}

				if (subX >= 0 && subX < tiles.length
						&& subY >= 0 && subY < tiles[subX].length
						&& tiles[subX][subY] != TileType.EMPTY) {
					// Sub vertex fixed

					int[] prevCur = prev[curX][curY][curDirect];

					int dMoveId = getMoveId(dx, dy);
					MoveProperty[] subMoves = MoveProperties[curDElem.direct][dMoveId];
					for (int j = 0; j < subMoves.length; j++) {
						int plusWeight = 0;
						if (prevCur[0] == subX && prevCur[1] == subY) {
							plusWeight = 7;
							if (curX == 13 && curY == 12)
								;
							// System.out.println(new CordPoint(prevCur[0],
							// prevCur[1]) + " <<< ");
						}
						if (!isMagicEdge) {
							plusWeight += 2;
						}
						int distToSub = dist[curX][curY][curDirect]
								+ subMoves[j].weight + plusWeight;
						if ((dist[subX][subY][subMoves[j].newPtr] == -1)
								|| (dist[subX][subY][subMoves[j].newPtr] > distToSub)) {
							dist[subX][subY][subMoves[j].newPtr] = distToSub;
							prev[subX][subY][subMoves[j].newPtr] = new int[] {
									curX, curY, curDirect };
							DElem newSubDe = new DElem(subX, subY,
									subMoves[j].newPtr, distToSub);
							d.add(newSubDe);
						}
					}
				}
			}
		}
		// TODO synchronize shit
		// DOTO shit synchronized
		// find max
		int bestEndDir = -1;
		for (int i = 0; i < dist[toPoint.x][toPoint.y].length; i++) {
			if (dist[toPoint.x][toPoint.y][i] != -1) {
				if ((bestEndDir == -1)
						|| (dist[toPoint.x][toPoint.y][i] < dist[toPoint.x][toPoint.y][bestEndDir])) {
					bestEndDir = i;
				}
			}
		}
		// max found
		if (dbgCnt1 < 4) {
			for (int i = 0; i < dist.length; i++) {
				for (int j = 0; j < dist[i].length; j++) {
					System.out.printf("{%d, %d, %d, %d}  ", dist[i][j][0],
							dist[i][j][6], dist[i][j][7], dist[i][j][9]);
				}
				System.out.println();
			}
			dbgCnt1++;
			// System.err.println("123");

		}
		int indX = toPoint.x;
		int indY = toPoint.y;
		int indDir = bestEndDir;
		ArrayList<CordPoint> reversedWay = new ArrayList<CordPoint>();
		while (!(indX == fromPoint.x && indY == fromPoint.y)) {
			// System.out.printf("%d %d %d\n", indX, indY, indDir);
			if ((indX == -1) || (indY == -1) || (indDir == -1)) {
				return -1;
			}
			reversedWay.add(new CordPoint(indX, indY));
			int[] prevProp = prev[indX][indY][indDir];
			indX = prevProp[0];
			indY = prevProp[1];
			indDir = prevProp[2];
		}
		Collections.reverse(reversedWay);
		for (int i = 0; i < reversedWay.size(); i++) {
			fullWay.add(reversedWay.get(i).getClone());
		}
		// return prev;
		return bestEndDir;
	}

	void addWayFromPrevs(CordPoint[][] prev, ArrayList<CordPoint> fullWay,
			CordPoint fromPoint, CordPoint toPoint) {

		CordPoint curP = toPoint.getClone();
		ArrayList<CordPoint> way = new ArrayList<CordPoint>();
		while (!curP.equals(fromPoint)) {
			way.add(curP);
			curP = prev[curP.x][curP.y];
		}
		for (int i = way.size() - 1; i > -1; i--) {
			fullWay.add(way.get(i));
		}
		// fullWay.add(new int[] { indX, indY });
		// TODO FFFF444DDD
	}

	boolean isTurnTile(TileType t) {
		return t == TileType.LEFT_BOTTOM_CORNER
				|| t == TileType.LEFT_TOP_CORNER
				|| t == TileType.RIGHT_BOTTOM_CORNER
				|| t == TileType.RIGHT_TOP_CORNER
				|| t == TileType.RIGHT_HEADED_T
				|| t == TileType.BOTTOM_HEADED_T || t == TileType.LEFT_HEADED_T
				|| t == TileType.TOP_HEADED_T;

	}

	boolean isLineTile(TileType t) {
		return t == TileType.VERTICAL || t == TileType.HORIZONTAL;
	}

	private boolean fullUnEqual(CordPoint p1, CordPoint p2) {
		return p1.x != p2.x && p1.y != p2.y;
	}

	private boolean isDiagTiles(CordPoint b1, CordPoint e1, CordPoint b2,
			CordPoint e2) {
		return (b1.x - e1.x == b2.x - e2.x) && (b1.y - e1.y == b2.y - e2.y);
	}

	Point getRealCenterCordPoint(CordPoint p, Game game) {
		return new Point((p.x + 0.5D) * game.getTrackTileSize(), (p.y + 0.5D)
				* game.getTrackTileSize());
	}

	TileType lastNextTile = null;
	TileType nextTurn[][];
	TileType prevTurn[][];
	boolean isInit = false;
	boolean isWallCrash = false;
	boolean isMapDelaySpecify = false;
	boolean nextTurnGenerate = false;
	boolean correctDriftSet = false;
	boolean useErrorDrift = true;
	int carsInTeam = 0;
	double minLevelBackward = 80;
	double levelBackward = minLevelBackward;
	double maxLevelBackward = 110;
	double startPlavHod = 0;
	int correctDriftX = 0;
	int correctDriftY = 0;
	double nextGenTurnX = 0;
	double nextGenTurnY = 0;
	int nextGenForX = 0;
	int nextGenForY = 0;
	int mapSpecifyDelay = 100;
	int wallCrashStart = 0;
	int repairTimeStart = 0;
	int curWaypointIndex = 0;
	double ONE = PI / 60;
	double brakeStopFactor = 0.15D;
	double slowdownFactor = 0.036D;
	double marginRectSideLength = 0;
	TileType[][] lastKnownTiles = null;
	CordPoint nextAssumeForPoint = null;
	NextAssumeState assumeStateForGen = null;
	TileType lastLineTile = null;

	void init(Car self, World world, Game game, Move move) {
		// TODO
		System.out.println(getBorder(new CordPoint(0, 1), new CordPoint(0, 2),
				game));
		System.out.println("mapName: " + world.getMapName());
		System.out.println("WayPoints:\n");
		for (int i = 0; i < world.getWaypoints().length; i++) {
			System.out.printf("{ %d %d }", world.getWaypoints()[i][0],
					world.getWaypoints()[i][1]);
		}
		System.out.println("-------");
		;
		marginRectSideLength = game.getTrackTileMargin();
		carWay.add(new CordPoint((int) (self.getX() / game.getTrackTileSize()),
				(int) (self.getY() / game.getTrackTileSize())));
		System.out.println(Geo.isInFigure(new Detail[] {
				new Detail(new Point(1, 1), new Point(1, -1)),
				new Detail(new Point(1, -1), new Point(-1, -1)),
				new Detail(new Point(-1, -1), new Point(-1, 1)),
				new Detail(new Point(-1, 1), new Point(1, 1)) }, new Point(0,
				10.1D)));
		System.out.printf("%f %f %f xFD\n",
				game.getCarCrosswiseMovementFrictionFactor(),
				game.getCarLengthwiseMovementFrictionFactor(),
				game.getCarMovementAirFrictionFactor());
		lastLineTile = world.getTilesXY()[world.getWaypoints()[0][0]][world
				.getWaypoints()[0][1]];
		// if (world.getMapName().equals("map03")) {
		// isMapDelaySpecify = true;
		// }
		TileType[][] tiles = world.getTilesXY();
		expectedDirect = new CordPoint[tiles.length][tiles[0].length];

		System.out.printf("%d %d %d %d\n", world.getWaypoints()[0][0],
				world.getWaypoints()[0][1],
				world.getWaypoints()[world.getWaypoints().length / 2][0],
				world.getWaypoints()[world.getWaypoints().length / 2][1]);
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				String s = tiles[i][j].toString();
				System.out.printf("%c%c%c", s.charAt(0), s.charAt(1),
						s.charAt(2));
				System.out.print(' ');
			}
			System.out.println();
		}
		// int[][] arr = bfs(tiles, world.getWaypoints()[0][0],
		// world.getWaypoints()[0][1],
		// world.getWaypoints()[world.getWaypoints().length / 2][0],
		// world.getWaypoints()[world.getWaypoints().length / 2][1]);
		// System.out.println("BFS TEST");
		// for (int i = 0; i < arr.length; i++) {
		// System.out.printf("%d %d\n", arr[i][0], arr[i][1]);
		// }

		for (Car car : world.getCars()) {
			if (car.isTeammate()) {
				carsInTeam++;
			}
		}
		if (carsInTeam == 2) {
			System.out.println("Cars in Team - 2");
		}
	}

	private enum NextAreaType {
		LINEAR, PREPARE_TO_DIAG, DIAG_MOVES, END_DIAG_TURN, PREPARE_TO_LONG_TURN, PREPARE_TO_SMALL_TURN, LONG_TURN, PREPARE_TO_SHARP_TURN, SHARP_TURN, END_DIAG_SHARP_TURN, SHARP_TURN_TO_DIAG, PREPARE_TO_REVERSE
	}

	private enum NextAssumeState {
		POST_DIAG,
	}

	private class NextTurnTile {
		// TODO
		TileType type;
		int x;
		int y;
		CordPoint nextTurn;
		CordPoint next2Tile;
		CordPoint collisionTile;
		Point next2Turn;
		Point factor;
		double nextRealX;
		double nextRealY;
		char speedStopCord;
		NextAreaType nextArea;
		public int subX;
		public int subY;
		public CordPoint subP;
		public CordPoint next2Point;
		public CordPoint next3Point;
		public CordPoint next4Point;
		public NextAssumeState assumeState;

		NextTurnTile(CordPoint nextTurn, Point factor, NextAreaType area) {
			this.x = nextTurn.x;
			this.y = nextTurn.y;
			this.factor = factor;
			this.nextArea = area;
		}

		NextTurnTile(CordPoint nextTurn, Point factor, char speedStopCord,
				NextAreaType area) {
			this.x = nextTurn.x;
			this.y = nextTurn.y;
			this.factor = factor;
			this.speedStopCord = speedStopCord;
			this.nextArea = area;
		}

		NextTurnTile(CordPoint nextTurn, Point nextReal, Point factor,
				char stopCord, NextAreaType area) {
			this.x = nextTurn.x;
			this.y = nextTurn.y;
			this.nextRealX = nextReal.x;
			this.nextRealY = nextReal.y;
			this.factor = factor;
			this.speedStopCord = stopCord;
			this.nextArea = area;
		}

		public NextTurnTile(CordPoint nextTurn, CordPoint next2Tile,
				CordPoint collisionTile, Point factor, char stopCord,
				NextAreaType nextArea) {
			// TODO Auto-generated constructor stub
			this.x = nextTurn.x;
			this.y = nextTurn.y;
			this.next2Tile = next2Tile;
			this.collisionTile = collisionTile;
			this.factor = factor;
			this.speedStopCord = stopCord;
			this.nextArea = nextArea;
		}

		NextTurnTile(CordPoint nextTurn, Point nextReal, Point next2Turn,
				Point factor, char stopCord, NextAreaType area) {
			this.x = nextTurn.x;
			this.y = nextTurn.y;
			this.nextRealX = nextReal.x;
			this.nextRealY = nextReal.y;
			this.factor = factor;
			this.speedStopCord = stopCord;
			this.next2Turn = next2Turn;
			this.nextArea = area;
		}
	}

	ArrayList<CordPoint> genWay = new ArrayList<CordPoint>();
	// may be <Cord3Point> pleas think
	int nextGenIndex;

	// @SuppressWarnings("unchecked")
	NextTurnTile getNextTurn(World world, Game game, Car car) {
		// TODO
		int carX = (int) (car.getX() / game.getTrackTileSize());
		int carY = (int) (car.getY() / game.getTrackTileSize());
		TileType[][] tiles = world.getTilesXY();
		CordPoint carPos = new CordPoint(
				(int) (car.getX() / game.getTrackTileSize()),
				(int) (car.getY() / game.getTrackTileSize()));
		int[][] MOVES = TileMove.MOVES;
		CordPoint minAngleToTile = null;
		for (int i = 0; i < MOVES.length; i++) {
			int dx = MOVES[i][0];
			int dy = MOVES[i][1];
			double rx = (((double) (carPos.x + dx)) + 0.5D)
					* game.getTrackTileSize();
			double ry = (((double) (carPos.y + dy)) + 0.5D)
					* game.getTrackTileSize();
			if (minAngleToTile == null
					|| Math.abs(car.getAngleTo(rx, ry)) < Math.abs(car
							.getAngleTo(
									(minAngleToTile.x + 0.5D)
											* game.getTrackTileSize(),
									(minAngleToTile.y + 0.5D)
											* game.getTrackTileSize()))) {
				minAngleToTile = new CordPoint(carPos.x + dx, carPos.y + dy);
			}
		}
		// if (carPos.x == 1 && carPos.y == 0) {
		// System.out.printf("%d %d <- minAngleTile\n", minAngleToTile.x,
		// minAngleToTile.y);
		// }
		CordPoint[][] prev = new CordPoint[tiles.length][];
		for (int i = 0; i < prev.length; i++) {
			prev[i] = new CordPoint[tiles[i].length];
		}
		CordPoint curP = new CordPoint(carX, carY);
		for (int i = carWay.size() - 2; i > -1; i--) {
			prev[curP.x][curP.y] = carWay.get(i).getClone();
			curP = carWay.get(i).getClone();
		}
		int nextInd = car.getNextWaypointIndex();
		int[][] wayPoints = world.getWaypoints();
		boolean canUseGenWay;
		if (genWay.size() > 1) {
			if (!genWay.get(0).equals(carPos)) {
				genWay.remove(0);
			}
			if (genWay.get(0).equals(carPos)) {
				canUseGenWay = true;
			} else {
				canUseGenWay = false;
			}
		} else {
			if (genWay.size() > 0) {
				canUseGenWay = genWay.get(0).equals(carPos);
			} else {
				canUseGenWay = false;
			}
		}
		ArrayList<CordPoint> fullWay = new ArrayList<CordPoint>();
		int toX = carX, toY = carY;

		CordPoint prevCarPoint = null;
		boolean canUnBfs = false;
		if (lastKnownTiles == null || !isEqual2DArrays(lastKnownTiles, tiles)) {
			lastKnownTiles = clonate2DArrays(tiles);
			canUseGenWay = false;
		}

		if (canUseGenWay) {
			fullWay = genWay;
			toX = wayPoints[nextGenIndex][0];
			toY = wayPoints[nextGenIndex][1];
			nextInd = Integer.valueOf(nextGenIndex);
			// nextInd++;
			// nextInd %= wayPoints.length;
			if (fullWay.size() > 1) {
				prevCarPoint = fullWay.get(fullWay.size() - 2).getClone();
			}
			canUnBfs = true;
		} else {
			if (carWay.size() > 1) {
				prevCarPoint = carWay.get(carWay.size() - 2).getClone();
			}
			fullWay.add(new CordPoint(carX, carY));
		}
		int wayLength = 5;
		CordPoint prevPoint = carWay.size() > 1 ? carWay.get(carWay.size() - 2)
				: null;
		// System.out.printf("%s -fuck\n", fullWay.toString());
		ArrayList<CordPoint> fullWayCopy = new ArrayList<CordPoint>();
		for (int i = 0; i < fullWay.size(); i++) {
			fullWayCopy.add(fullWay.get(i).getClone());
		}
		int lastDirect;
		if (carWay.size() > 1) {
			lastDirect = getDirectId(carWay.get(carWay.size() - 2),
					carWay.get(carWay.size() - 1));
		} else {
			lastDirect = 3;
		}
		// System.out.printf("%d <- start Direction\n", lastDirect);
		for (int cnter = 0; cnter < 1000; cnter++) {

			System.out.println(fullWay);
			fullWay = (ArrayList<CordPoint>) fullWayCopy.clone();// copified
			if (canUnBfs) {
				canUnBfs = false;
				// System.out.println("hello");
			} else {
				// System.out.printf("%d %d %d %d - from, to\n", toX, toY,
				// wayPoints[nextInd][0], wayPoints[nextInd][1]);
				lastDirect = bfs(tiles, new CordPoint(toX, toY), new CordPoint(
						wayPoints[nextInd][0], wayPoints[nextInd][1]),
						lastDirect, fullWay, world, prevCarPoint);
				if (fullWay.size() > 1) {
					prevCarPoint = fullWay.get(fullWay.size() - 2).getClone();
				}
				if (lastDirect == -1) {
					break;
				}
				genWay = fullWay;
				nextGenIndex = Integer.valueOf(nextInd);
				toX = wayPoints[nextInd][0];
				toY = wayPoints[nextInd][1];
			}
			// nextGenIndex++
			// addWayFromPrevs(prev, fullWay, new CordPoint(toX, toY),
			// new CordPoint(wayPoints[nextInd][0], wayPoints[nextInd][1]));
			fullWayCopy = new ArrayList<CordPoint>();
			for (int i = 0; i < fullWay.size(); i++) {
				fullWayCopy.add(fullWay.get(i).getClone());
			}
			// System.out.printf("carPos : %d %d\n", carX, carY);
			// System.out.println(fullWay);
			for (int i = 0; i < fullWay.size() - wayLength; i++) {
				int curX = fullWay.get(i).x, curY = fullWay.get(i).y;
				int subX = fullWay.get(i + 2).x, subY = fullWay.get(i + 2).y;
				int cur2X = fullWay.get(i + 1).x, cur2Y = fullWay.get(i + 1).y;
				int sub2X = fullWay.get(i + 3).x, sub2Y = fullWay.get(i + 3).y;
				CordPoint[] way = new CordPoint[wayLength];

				for (int j = i; j < i + wayLength; j++) {
					way[j - i] = new CordPoint(fullWay.get(j).x,
							fullWay.get(j).y);
				}
				if ((curX != subX && curY != subY)) {
					if (cur2X != sub2X && cur2Y != sub2Y
							&& cur2X - sub2X == curX - subX
							&& cur2Y - sub2Y == curY - subY) {
						double curRealX = (curX + 0.5D)
								* game.getTrackTileSize();
						double curRealY = (curY + 0.5D)
								* game.getTrackTileSize();
						double cur2RealX = (cur2X + 0.5D)
								* game.getTrackTileSize();
						double cur2RealY = (cur2Y + 0.5D)
								* game.getTrackTileSize();
						double subRealX = (subX + 0.5D)
								* game.getTrackTileSize();
						double subRealY = (subY + 0.5D)
								* game.getTrackTileSize();
						double sub2RealX = (sub2X + 0.5D)
								* game.getTrackTileSize();
						double sub2RealY = (sub2Y + 0.5D)
								* game.getTrackTileSize();
						nextTurnGenerate = true;
						// nextGenTurnX = (subRealX + sub2RealX) / 2;
						// nextGenTurnY = (subRealY + sub2RealY) / 2;
						nextGenTurnX = (cur2RealX + subRealX) / 2;
						nextGenTurnY = (cur2RealY + subRealY) / 2;
						nextGenForX = cur2X;
						nextGenForY = cur2Y;
						// System.out.printf("%f %f * %f %f\n", cur2RealX,
						// cur2RealY, subRealX, subRealY);
						NextAreaType nextArea = null;
						boolean inOneLine = false;
						if (prevPoint != null) {
							if ((prevPoint.x == curX && curX == cur2X)
									|| (prevPoint.y == curY && curY == cur2Y)) {
								inOneLine = true;
							}
						} else {
							inOneLine = true;
						}
						Point nextTurnPoint = null;

						if (carX == curX && carY == curY) {
							if (prevPoint != null
									&& isDiagTiles(prevPoint, way[0], way[1],
											way[2])) {
								nextTurnPoint = new Point(
										(curRealX + cur2RealX) / 2,
										(curRealY + cur2RealY) / 2);
								nextArea = NextAreaType.DIAG_MOVES;
							} else if (inOneLine) {
								nextTurnPoint = new Point(
										(cur2RealX + subRealX) / 2,
										(cur2RealY + subRealY) / 2);
								nextArea = NextAreaType.PREPARE_TO_DIAG;
							} else {
								nextArea = NextAreaType.SHARP_TURN_TO_DIAG;
							}

						} else {
							nextArea = NextAreaType.PREPARE_TO_DIAG;
						}
						int tx = fullWay.get(i + 1).x, ty = fullWay.get(i + 1).y;
						// System.out.printf(
						// "carPos:{%d %d}, next:{%d %d}, {%d %d}\n",
						// carX, carY, curX, curY, cur2X, cur2Y);
						double factorX;
						double factorY;
						int preTCurX = curX - tx;
						int preTCurY = curY - ty;
						int preTSubX = subX - tx;
						int preTSubY = subY - ty;
						factorX = preTCurX + preTSubX;
						factorY = preTCurY + preTSubY;
						char stopCord;
						if (curX - tx == 0) {
							stopCord = 'X';
						} else {
							stopCord = 'Y';
						}
						NextTurnTile t = new NextTurnTile(new CordPoint(curX,
								curY), new Point((cur2RealX + subRealX) / 2,
								(cur2RealY + subRealY) / 2), new Point(factorX,
								factorY), stopCord, nextArea);
						// t.subX = subX;
						// t.subY = subY;
						t.next2Point = new CordPoint(cur2X, cur2Y);
						t.next3Point = new CordPoint(subX, subY);
						t.next4Point = new CordPoint(sub2X, sub2Y);
						t.collisionTile = new CordPoint(cur2X + (int) factorX,
								cur2Y + (int) factorY);

						if (nextAssumeForPoint != null
								&& carPos.equals(nextAssumeForPoint)) {
							t.assumeState = assumeStateForGen;
						} else {
							nextAssumeForPoint = null;
						}
						return t;
					} else {
						boolean isSharpTurn = false;
						if (cur2X != sub2X && cur2Y != sub2Y
								&& curX - cur2X == sub2X - subX
								&& curY - cur2Y == sub2Y - subY) {
							isSharpTurn = true;
							// System.out.println(nextArea);//TODO unwrite
						}

						if (nextTurnGenerate && curX == nextGenForX
								&& curY == nextGenForY) {
							double cur2RealX = (subX + 0.5D)
									* game.getTrackTileSize();
							double cur2RealY = (subY + 0.5D)
									* game.getTrackTileSize();

							double sub2RealX = (sub2X + 0.5D)
									* game.getTrackTileSize();
							double sub2RealY = (sub2Y + 0.5D)
									* game.getTrackTileSize();
							double next2RealX = (cur2RealX + sub2RealX) / 2;
							double next2RealY = (cur2RealY + sub2RealY) / 2;
							NextAreaType nextArea = (isSharpTurn) ? NextAreaType.END_DIAG_SHARP_TURN
									: NextAreaType.END_DIAG_TURN;
							NextTurnTile t = new NextTurnTile(new CordPoint(
									curX, curY), new Point(nextGenTurnX,
									nextGenTurnY), new Point(next2RealX,
									next2RealY), new Point(0, 0), 'U', nextArea);

							nextAssumeForPoint = new CordPoint(cur2X, cur2Y);
							assumeStateForGen = NextAssumeState.POST_DIAG;
							t.next2Point = new CordPoint(cur2X, cur2Y);
							t.next3Point = new CordPoint(subX, subY);
							t.next4Point = new CordPoint(sub2X, sub2Y);
							return t;
						}
						nextTurnGenerate = false;
						int tx = fullWay.get(i + 1).x, ty = fullWay.get(i + 1).y;
						char stopCord;
						if (curX - tx == 0) {
							stopCord = 'X';
						} else {
							stopCord = 'Y';
						}
						NextAreaType nextArea;
						if (carX == curX && carY == curY) {
							nextArea = NextAreaType.LONG_TURN;
						} else {
							nextArea = NextAreaType.PREPARE_TO_LONG_TURN;
						}
						int cur3X = fullWay.get(i + 3).x, cur3Y = fullWay
								.get(i + 3).y;
						int sub3X = fullWay.get(i + 4).x, sub3Y = fullWay
								.get(i + 4).y;

						if (curX - tx == cur3X - sub3X
								&& curY - ty == cur3Y - sub3Y) {
							nextArea = NextAreaType.PREPARE_TO_SMALL_TURN;
							// System.out.println("Prepared");
						}

						// System.out.printf("%d %d %d %d\n", curX, curY, sub3X,
						// sub3Y);
						CordPoint next2Tile = new CordPoint(subX, subY);
						double factorX = 0;
						double factorY = 0;
						curX -= tx;
						curY -= ty;
						subX -= tx;
						subY -= ty;
						factorX = curX + subX;
						factorY = curY + subY;
						CordPoint collisionTile = new CordPoint(tx
								+ (int) factorX, ty + (int) factorY);
						NextTurnTile t = new NextTurnTile(
								new CordPoint(tx, ty), next2Tile,
								collisionTile, new Point(factorX, factorY),
								stopCord, nextArea);
						if (nextAssumeForPoint != null
								&& carPos.equals(nextAssumeForPoint)) {
							t.assumeState = assumeStateForGen;
						} else {
							nextAssumeForPoint = null;
						}
						return t;
					}
				} else if(curX == subX && curY == subY) {
					char stopCord;
					Point factor;
					if (curX - cur2X == 0) {
						stopCord = 'X';
						factor = new Point(1, 0);
					} else {
						stopCord = 'Y';
						factor = new Point(0, 1);
					}
					CordPoint next2Tile = new CordPoint(subX, subY);
					NextTurnTile t = new NextTurnTile(
							new CordPoint(cur2X, cur2Y), next2Tile,
							null, factor,
							stopCord, NextAreaType.PREPARE_TO_REVERSE);
					return t;
				}
			}
			nextInd++;
			nextInd %= wayPoints.length;

		}
		System.out.println("Waht?");
		return new NextTurnTile(new CordPoint(car.getNextWaypointX(),
				car.getNextWaypointY()), new Point(0, 0), NextAreaType.LINEAR);
	}

	int cnt = 0;
	int cnt2 = 0;

	boolean isEqual2DArrays(TileType[][] arr1, TileType[][] arr2) {
		for (int i = 0; i < arr1.length; i++) {
			for (int j = 0; j < arr1[i].length; j++) {
				if (arr1[i][j] != arr2[i][j]) {
					return false;
				}
			}
		}
		return true;
	}

	TileType[][] clonate2DArrays(TileType[][] arr) {
		TileType[][] cloneArr = new TileType[arr.length][];
		for (int i = 0; i < arr.length; i++) {
			cloneArr[i] = new TileType[arr[i].length];
			for (int j = 0; j < arr[i].length; j++) {
				cloneArr[i][j] = arr[i][j];
			}
		}
		return cloneArr;
	}

	Detail[] getQuadrateDetails(CordPoint p, Game game) {
		Point p1 = new Point(((double) p.x) * game.getTrackTileSize(),
				((double) p.y) * game.getTrackTileSize());
		Point p2 = new Point(((double) (p.x + 1)) * game.getTrackTileSize(),
				((double) p.y) * game.getTrackTileSize());
		Point p3 = new Point(((double) (p.x + 1)) * game.getTrackTileSize(),
				((double) (p.y + 1)) * game.getTrackTileSize());
		Point p4 = new Point(((double) p.x) * game.getTrackTileSize(),
				((double) (p.y + 1)) * game.getTrackTileSize());
		return new Detail[] { new Detail(p1, p2), new Detail(p2, p3),
				new Detail(p3, p4), new Detail(p4, p1) };
	}

	Detail[] getQuadrateDetails(Point center, double sideLength) {
		double zdvig = sideLength / 2;
		Point p1 = new Point(((double) center.x) - (zdvig), ((double) center.y)
				+ zdvig);
		Point p2 = new Point(((double) (center.x)) + zdvig, ((double) center.y)
				+ zdvig);
		Point p3 = new Point(((double) (center.x)) + zdvig,
				((double) (center.y)) - zdvig);
		Point p4 = new Point(((double) center.x) - zdvig, ((double) (center.y))
				- zdvig);
		return new Detail[] { new Detail(p1, p2), new Detail(p2, p3),
				new Detail(p3, p4), new Detail(p4, p1) };
	}

	boolean canShoot(Car self, Car target, Game game, World world) {
		TileType[][] tiles = world.getTilesXY();
		int[][] allMoves = TileMove.MOVES;
		Detail projectileVector = new Detail(
				new Point(self.getX(), self.getY()), new Point(target.getX(),
						target.getY()));
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				CordPoint cur = new CordPoint(i, j);// cur fixed
				int[][] allowedMoves = TileMove.getMoves(tiles[i][j]);
				for (int t = 0; t < allMoves.length; t++) {
					boolean isWall = false;
					for (int k = 0; k < allowedMoves.length; k++) {
						if (allowedMoves[k][0] == allMoves[t][0]
								&& allowedMoves[k][1] == allMoves[t][1]) {
							isWall = true;
							break;
						}
					}
					if (isWall) {
						continue;
					}
					CordPoint sub = new CordPoint(cur.x + allMoves[t][0], cur.y
							+ allMoves[t][1]);
					Detail border = getBorder(cur, sub, game);
					if (Geo.isInter(projectileVector, border)) {
						return false;
					}

					Detail[] details = getQuadrateDetails(cur.getClone(), game);
					for (Detail d : details) {
						Detail[] marginRect = getQuadrateDetails(
								d.point1.getClone(), marginRectSideLength);
						if (Geo.isInterDetails(
								new Detail[] { projectileVector.getClone() },
								marginRect)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	Detail getBorder(CordPoint p1, CordPoint p2, Game game) {
		assert p1.x == p2.x || p1.y == p2.y;
		if (p1.y == p2.y) {
			if (p1.x > p2.x) {
				CordPoint tmp = p1.getClone();
				p1 = p2.getClone();
				p2 = tmp.getClone();
			}
			double x = (double) p2.x * game.getTrackTileSize();
			double y = (double) p2.y * game.getTrackTileSize();
			return new Detail(new Point(x, y), new Point(x, y
					+ game.getTrackTileSize()));
		} else {
			if (p1.y > p2.y) {
				CordPoint tmp = p1.getClone();
				p1 = p2.getClone();
				p2 = tmp.getClone();
			}
			double x = (double) p2.x * game.getTrackTileSize();
			double y = (double) p2.y * game.getTrackTileSize();
			return new Detail(new Point(x, y), new Point(x
					+ game.getTrackTileSize(), y));
		}
	}

}

class Geo {
	public static double one = Math.PI / 180;

	public static Point[] arrToPoint(int[][] arr) {
		Point[] rez = new Point[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].length == 2)
				rez[i] = new Point(arr[i][0], arr[i][1]);
			else if (arr[i].length == 3)
				System.out.println("invalid property point cant be circle");
		}
		rez[rez.length - 1] = new Point(arr[0][0], arr[0][1]);
		return rez;
	}

	public static Detail[] arrToDetail(int[][] arr) {
		int count = 0;
		int i = 0;
		while (i < arr.length) {
			if (arr[i].length == 2) {
				count++;
				i += 1;
			} else if (arr[i].length == 3) {
				count++;
				i++;
			}
		}
		Detail[] rez = new Detail[count];
		i = 0;
		int n = 0;
		while (i < arr.length) {
			if (arr[i].length == 2) {
				if (i + 1 < arr.length) {
					rez[n] = new Detail(arr[i][0], arr[i][1], arr[i + 1][0],
							arr[i + 1][1]);
				} else {
					rez[n] = new Detail(arr[i][0], arr[i][1], arr[0][0],
							arr[0][1]);
				}
				i++;
				n++;
			} else if (arr[i].length == 3) {
				rez[n] = new Detail(arr[i][0], arr[i][1], arr[i][2]);
				n++;
				i++;
			}
		}
		return rez;
	}

	public static Detail[] arrToDetail(double[][] arr) {
		int count = 0;
		int i = 0;
		while (i < arr.length) {
			if (arr[i].length == 2) {
				count++;
				i += 1;
			} else if (arr[i].length == 3) {
				count++;
				i++;
			}
		}
		Detail[] rez = new Detail[count];
		i = 0;
		int n = 0;
		while (i < arr.length) {
			if (arr[i].length == 2) {
				if (i + 1 < arr.length) {
					rez[n] = new Detail(arr[i][0], arr[i][1], arr[i + 1][0],
							arr[i + 1][1]);
				} else {
					rez[n] = new Detail(arr[i][0], arr[i][1], arr[0][0],
							arr[0][1]);
				}
				i++;
				n++;
			} else if (arr[i].length == 3) {
				rez[n] = new Detail(arr[i][0], arr[i][1], arr[i][2]);
				n++;
				i++;
			}
		}
		return rez;
	}

	public static Detail[] arrToDetailDisconnect(double[][] arr) {
		Detail[] rez = new Detail[arr.length / 2];
		int n = 0;
		for (int i = 0; i < arr.length; i += 2) {
			if (arr[i].length == 2) {
				rez[n] = new Detail(arr[i][0], arr[i][1], arr[i + 1][0],
						arr[i + 1][1]);

			} else if (arr[i].length == 3) {
				rez[n] = new Detail(arr[i][0], arr[i][1], arr[i][2]);
			}
			n++;
		}
		return rez;
	}

	public static Detail[] mergeDetailsArrays(Detail[] arr1, Detail[] arr2) {
		Detail[] rez = new Detail[arr1.length + arr2.length];
		for (int i = 0; i < arr1.length; i++) {
			rez[i] = arr1[i];
		}
		for (int i = 0; i < arr2.length; i++) {
			rez[i + arr1.length] = arr2[i];
		}
		return rez;
	}

	public static double[][] mergeArrs(double[][] arr1, double[][] arr2) {
		double[][] rez = new double[arr1.length + arr2.length][2];
		for (int i = 0; i < arr1.length; i++) {
			rez[i] = arr1[i];
		}
		for (int i = 0; i < arr2.length; i++) {
			rez[i + arr1.length] = arr2[i];
		}
		return rez;
	}

	public static double getDistanceToPoint(double x, double y) {
		return Math.sqrt((x * x) + (y * y));
	}

	public static Point toCord(double direct, double distance) {
		return new Point(distance * Math.cos(direct), distance
				* Math.sin(direct));
	}

	public static double[][] scallArr(double[][] arr, double scall) {
		for (int i = 0; i < arr.length; i++) {
			arr[i][0] *= scall;
			arr[i][1] *= scall;
		}
		return arr;
	}

	public static boolean isInter(Detail d1, Detail d2) {
		double interX;
		double interY;
		double a1 = (d1.point1.y - d1.point2.y) / (d1.point1.x - d1.point2.x);
		double a2 = (d2.point1.y - d2.point2.y) / (d2.point1.x - d2.point2.x);

		double b1 = d1.point1.y - (a1 * d1.point1.x);
		double b2 = d2.point1.y - (a2 * d2.point1.x);

		if (a1 == a2) {
			return false;
		}

		if (d1.point1.x == d1.point2.x) {
			interX = d1.point1.x;
			interY = (interX * a2) + b2;
		} else if (d2.point1.x == d2.point2.x) {
			interX = d2.point1.x;
			interY = (interX * a1) + b1;
		} else {
			interX = (b2 - b1) / (a1 - a2);
			interY = (interX * a1) + b1;
		}

		// System.out.printf("%f, %f\n", interX, interY);

		// sadasdasdasdasdasdasdasdфываппро
		double d1x1 = Math.min(d1.point1.x, d1.point2.x), d1x2 = Math.max(
				d1.point1.x, d1.point2.x);
		double d2x1 = Math.min(d2.point1.x, d2.point2.x), d2x2 = Math.max(
				d2.point1.x, d2.point2.x);
		double d1y1 = Math.min(d1.point1.y, d1.point2.y), d1y2 = Math.max(
				d1.point1.y, d1.point2.y);
		double d2y1 = Math.min(d2.point1.y, d2.point2.y), d2y2 = Math.max(
				d2.point1.y, d2.point2.y);
		// System.out.printf("%f, %f, %f, %f\n", d2x1, d2y1, d2x2, d2y2);
		boolean isOk = (((d1x1 <= interX) && (interX <= d1x2)) && ((d1y1 <= interY) && (interY <= d1y2)))
				&& (((d2x1 <= interX) && (interX <= d2x2)) && ((d2y1 <= interY) && (interY <= d2y2)));

		// double d1x1 = Math.min(d1.point1.x, d1.point2.x), d1x2 =
		// Math.max(d1.point1.x, d1.point2.x);
		// double d2x1 = Math.min(d2.point1.x, d2.point2.x), d2x2 =
		// Math.max(d2.point1.x, d2.point2.x);
		// boolean xInter = (((d1x1 <= d2x1) && (d2x1 <= d1x2)) || ((d1x1 <=
		// d2x2) && (d2x2 <= d1x2)))
		// || (((d2x1 <= d1x1) && (d1x1 <= d2x2)) || ((d2x1 <= d1x2) && (d1x2 <=
		// d2x2)));
		// //System.out.printf("%f, %f, %f, %f\n", d1.point1.x, d1.point1.y,
		// d1.point2.x, d1.point2.y);
		// //System.out.printf("%f, %f, %f, %f\n", d2.point1.x, d2.point1.y,
		// d2.point2.x, d2.point2.y);
		//
		// double d1y1 = Math.min(d1.point1.y, d1.point2.y), d1y2 =
		// Math.max(d1.point1.y, d1.point2.y);
		// double d2y1 = Math.min(d2.point1.y, d2.point2.y), d2y2 =
		// Math.max(d2.point1.y, d2.point2.y);
		// boolean yInter = (((d1y1 <= d2y1) && (d2y1 <= d1y2)) || ((d1y1 <=
		// d2y2) && (d2y2 <= d1y2)))
		// || (((d2y1 <= d1y1) && (d1y1 <= d2y2)) || ((d2y1 <= d1y2) && (d1y2 <=
		// d2y2)));
		return isOk;
	}

	public static Point getInterPoint(Detail d1, Detail d2) {
		double interX;
		double interY;
		double a1 = (d1.point1.y - d1.point2.y) / (d1.point1.x - d1.point2.x);
		double a2 = (d2.point1.y - d2.point2.y) / (d2.point1.x - d2.point2.x);

		double b1 = d1.point1.y - (a1 * d1.point1.x);
		double b2 = d2.point1.y - (a2 * d2.point1.x);

		if (a1 == a2) {
			return null;
		}

		if (d1.point1.x == d1.point2.x) {
			interX = d1.point1.x;
			interY = (interX * a2) + b2;
		} else if (d2.point1.x == d2.point2.x) {
			interX = d2.point1.x;
			interY = (interX * a1) + b1;
		} else {
			interX = (b2 - b1) / (a1 - a2);
			interY = (interX * a1) + b1;
		}
		return new Point(interX, interY);
	}

	public static boolean isInterRays(Detail d1, Detail d2) {
		Point assumeInter = getInterPoint(d1, d2);
		if (assumeInter == null) {
			return false;
		}
		if (d1.point1.x < d1.point2.x) {
			if (d2.point1.x < d2.point2.x) {
				return assumeInter.x >= max(d1.point1.x, d2.point1.x);
			} else {
				return assumeInter.x >= max(d1.point1.x, d2.point2.x)
						&& assumeInter.x <= min(d1.point2.x, d2.point1.x);
			}
		} else {
			if (d2.point1.x < d2.point2.x) {
				return assumeInter.x >= max(d1.point2.x, d2.point1.x)
						&& assumeInter.x <= min(d1.point1.x, d2.point2.x);
			} else {
				return assumeInter.x <= min(d1.point1.x, d2.point1.x);
			}
		}
	}

	public static boolean isCommonPoint(Detail d1, Detail d2) {
		return (d1.point1.x == d2.point1.x && d1.point1.y == d2.point1.y)
				|| (d1.point1.x == d2.point2.x && d1.point1.y == d2.point2.y)
				|| (d1.point2.x == d2.point1.x && d1.point2.y == d2.point1.y)
				|| (d1.point2.x == d2.point2.x && d1.point2.y == d2.point2.y);
	}

	public static Point getCommonPoint(Detail d1, Detail d2) {
		if (d1.point1.x == d2.point1.x && d1.point1.y == d2.point1.y) {
			return new Point(d1.point1.x, d1.point1.y);
		} else if (d1.point1.x == d2.point2.x && d1.point1.y == d2.point2.y) {
			return new Point(d1.point1.x, d1.point1.y);
		} else {
			return new Point(d1.point2.x, d1.point2.y);
		}
	}

	public static Point get90AnglePoint(Detail d, Point center) {

		Point p = new Point(d.point2.x - d.point1.x, d.point2.y - d.point1.y);
		double c = Math.abs(p.distance);
		double a = Math.abs(new Point(d.point2.x - center.x, d.point2.y
				- center.y).distance);
		double b = Math.abs(new Point(d.point1.x - center.x, d.point1.y
				- center.y).distance);
		double dist = ((a * a) - (b * b) + (c * c)) / (2 * c);
		Point rez = Geo.toCord(p.direct, p.distance - dist);
		rez.setPos(rez.x + d.point1.x, rez.y + d.point1.y);
		return rez;
		/*
		 * double angle1 = new Point(d.point2.x - d.point1.x, d.point2.y -
		 * d.point1.y).direct; double angle2 = new Point(center.x - d.point1.x,
		 * center.y - d.point1.y).direct; double angle =
		 * Math.min(Math.toRadians(360) - Math.abs(angle1 - angle2),
		 * Math.abs(angle1 - angle2)); Engine.info = "" + Math.toDegrees(angle)
		 * ; angle = Math.toRadians(90) - angle; Engine.info += " " +
		 * Math.toDegrees(angle) + " "; Point p2 = new Point(d.point1.x -
		 * center.x, d.point1.y - center.y); angle = p2.direct - angle; Point p
		 * = toCord(angle, 2);
		 * 
		 * return getInterPoint(d, new Detail(center.x, center.y, center.x +
		 * p.x, center.y + p.y));
		 */
	}

	public static boolean isInterDetails(Detail[] ds1, Detail[] ds2) {
		for (Detail d1 : ds1) {
			for (Detail d2 : ds2) {
				if (Geo.isInter(d1, d2)) {
					return true;
				}
			}
		}
		return false;
	}

	public static double getVectorsAngle(Point a, Point b) {
		return abs(a.direct - b.direct);
		// double scallarr = (a.x * a.y) + (b.x * b.y);
		// return scallarr / (a.distance * b.distance);
	}

	public static boolean isInFigure(Detail[] details, Point p) {
		double calcAngle = 0;
		double calcAngle2 = 0;
		for (Detail d : details) {
			Point preP1 = new Point(d.point1.x - p.x, d.point1.y - p.y);
			Point preP2 = new Point(d.point2.x - p.x, d.point2.y - p.y);
			double vecAng = getVectorsAngle(d.point1, p);
			calcAngle2 += vecAng;
			System.out.printf("%f <- angle\n", preP1.direct - preP2.direct);
			calcAngle -= (preP1.direct - preP2.direct);
		}
		// calcAngle += (new Point(figure[0].x - p.x, figure[0].y -
		// p.y)).direct;

		System.out.println(calcAngle);
		return (abs(calcAngle) > 2);
	}

	public static Point[] getLeftRightRectBounds(CordPoint carPos,
			CordPoint nextWaypoint, Game game) {
		if (carPos.x == nextWaypoint.x) {
			if (carPos.y < nextWaypoint.y) {
				return new Point[] {
						new Point((carPos.x) * game.getTrackTileSize(),
								(carPos.y + 1) * game.getTrackTileSize()),
						new Point((nextWaypoint.x + 1)
								* game.getTrackTileSize(),
								((nextWaypoint.y) * game.getTrackTileSize())) };
			} else {
				return new Point[] {
						new Point(
								nextWaypoint.x * game.getTrackTileSize(),
								((nextWaypoint.y + 1) * game.getTrackTileSize())),
						new Point((carPos.x + 1) * game.getTrackTileSize(),
								(carPos.y) * game.getTrackTileSize()), };
			}
		} else if (carPos.x < nextWaypoint.x) {
			return new Point[] {
					new Point((carPos.x + 1) * game.getTrackTileSize(),
							carPos.y * game.getTrackTileSize()),
					new Point((nextWaypoint.x) * game.getTrackTileSize(),
							((nextWaypoint.y + 1) * game.getTrackTileSize())) };
		} else {
			return new Point[] {
					new Point((nextWaypoint.x + 1) * game.getTrackTileSize(),
							(nextWaypoint.y * game.getTrackTileSize())),
					new Point((carPos.x) * game.getTrackTileSize(),
							(carPos.y + 1) * game.getTrackTileSize()) };
		}

	}

	public static boolean isInRect(Point r1, Point r2, Point p) {
		return p.x > r1.x && p.y > r1.y && p.x < r2.x && p.y < r2.y;
	}
}

class CordPoint {
	int x, y;

	CordPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean equals(CordPoint p) {
		return p.x == this.x && p.y == this.y;
	}

	public String toString() {
		return "{ " + this.x + ", " + this.y + " }";
	}

	public CordPoint getClone() {
		return new CordPoint(this.x, this.y);
	}

	public CordPoint substract(CordPoint p) {
		return new CordPoint(this.x - p.x, this.y - p.y);
	}

	public CordPoint add(CordPoint p) {
		return new CordPoint(this.x + p.x, this.y + p.y);
	}
}

class Point {
	double x;
	double y;
	double direct;
	double distance;

	Point(double x, double y) {
		this.x = x;
		this.y = y;
		reloadCord();
	}

	public Point getSubstract(Point point) {
		// TODO Auto-generated method stub
		return new Point(this.x - point.x, this.y - point.y);
	}

	public Point(CordPoint p) {
		this.x = p.x;
		this.y = p.y;
		reloadCord();
	}

	public Point getClone() {
		return new Point((this.x), (this.y));
	}

	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
		reloadCord();
	}

	private void reloadCord() {
		if (x == 0 && y == 0) {
			this.distance = 0;
			this.direct = 0;
		} else {
			this.distance = Math.sqrt((x * x) + (y * y));
			this.direct = Math.acos(x / distance);
			if (y < 0) {
				this.direct *= -1;
			}
		}

		// System.out.printf("%f %f %f, %fl\n", x, y, direct, distance);

	}

	public Point getMiddle(Point p) {
		return new Point((this.x + p.x) / 2, (this.y + p.y) / 2);
	}

	public void setPos(Point p) {
		// TODO Auto-generated method stub
		this.x = p.x;
		this.y = p.y;
		reloadCord();
	}

	public Point add(Point p) {
		return new Point(this.x + p.x, this.y + p.y);
	}

	public static Point valueOf(CordPoint p) {
		return new Point(p.x, p.y);
	}

	public String toString() {
		return "{ " + this.x + ", " + this.y + " }";
	}
}

class Detail {
	Point point1;
	Point point2;
	boolean canCreate = true;
	boolean isCircle = false;
	double radius;

	public Detail(double x1, double y1, double x2, double y2) {
		this.point1 = new Point(x1, y1);
		this.point2 = new Point(x2, y2);
	}

	public Detail(double x, double y, double radius) {
		this.isCircle = true;
		this.point1 = new Point(x, y);
		this.radius = radius;
	}

	public Detail(boolean canCreate) {
		this.canCreate = canCreate;
	}

	public Detail(Point p1, Point p2) {
		this.point1 = new Point(p1.x, p1.y);
		this.point2 = new Point(p2.x, p2.y);
	}

	public String toString() {
		return "{ " + point1.toString() + ", " + point2.toString() + " }";
	}

	public Detail getClone() {
		return new Detail(this.point1.getClone(), this.point2.getClone());
	}
}
