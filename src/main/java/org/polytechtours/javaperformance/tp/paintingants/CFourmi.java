package org.polytechtours.javaperformance.tp.paintingants;
// package PaintingAnts_v3;

// version : 4.0

import java.awt.Color;
import java.util.Random;

public class CFourmi {
  // Tableau des incrémentations à effectuer sur la position des fourmis
  // en fonction de la direction du deplacement
  private final static int[][] mIncDirection = { { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 },
      { -1, 0 }, { -1, -1 } };
  // le generateur aléatoire (Random est thread safe donc on la partage)
  private final static Random GenerateurAleatoire = new Random();
  // couleur déposé par la fourmi
  private final Color mCouleurDeposee;
  private final float mLuminanceCouleurSuivie;
  // objet graphique sur lequel les fourmis peuvent peindre
  private final CPainting mPainting;
  // Coordonées de la fourmi
  private int x, y;
  // Proba d'aller a gauche, en face, a droite, de suivre la couleur
  private final float[] mProba = new float[4];
  // Numéro de la direction dans laquelle la fourmi regarde
  private byte mDirection;
  // Taille de la trace de phéromones déposée par la fourmi
  private final byte mTaille;
  // Pas d'incrémentation des directions suivant le nombre de directions
  // allouées à la fourmies
  private final byte mDecalDir;
  // l'applet
  private final PaintingAnts mApplis;
  // seuil de luminance pour la détection de la couleur recherchée
  private final float mSeuilLuminance;
  // nombre de déplacements de la fourmi
  private long mNbDeplacements;

  /*************************************************************************************************
  */
  public CFourmi(Color pCouleurDeposee, Color pCouleurSuivie, float pProbaTD, float pProbaG, float pProbaD,
      float pProbaSuivre, CPainting pPainting, char pTypeDeplacement, float pInit_x, float pInit_y, byte pInitDirection,
      byte pTaille, float pSeuilLuminance, PaintingAnts pApplis) {

    mCouleurDeposee = pCouleurDeposee;
    mLuminanceCouleurSuivie = 0.2126f * pCouleurDeposee.getRed() + 0.7152f * pCouleurDeposee.getGreen()
        + 0.0722f * pCouleurDeposee.getBlue();
    mPainting = pPainting;
    mApplis = pApplis;

    // direction de départ
    mDirection = pInitDirection;

    // taille du trait
    mTaille = pTaille;

    // initialisation des probas
    mProba[0] = pProbaG; // proba d'aller à gauche
    mProba[1] = pProbaTD; // proba d'aller tout droit
    mProba[2] = pProbaD; // proba d'aller à droite
    mProba[3] = pProbaSuivre; // proba de suivre la couleur

    // nombre de directions pouvant être prises : 2 types de déplacement
    // possibles
    if (pTypeDeplacement == 'd') {
      mDecalDir = 2;
    } else {
      mDecalDir = 1;
    }

    mSeuilLuminance = pSeuilLuminance;
    mNbDeplacements = 0;
  }

  /*************************************************************************************************
   * Titre : void deplacer() 
   * Description : Fonction de déplacement de la fourmi
   *
   */
  public synchronized void deplacer() {
    float tirage, prob1, prob2, prob3, total;
    byte[] dir = { 0, 0, 0 };
    int i, j;
    byte k;

    mNbDeplacements++;

    // le tableau dir contient 0 si la direction concernée ne contient pas la
    // couleur
    // à suivre, et 1 sinon (dir[0]=gauche, dir[1]=tt_droit, dir[2]=droite)
    for (k = -1; k <= 1; k++) {
      byte kek = (byte) (modulo(mDirection + (k * mDecalDir), 8));
      i = modulo(x + CFourmi.mIncDirection[kek][0], mPainting.getLargeur());
      j = modulo(y + CFourmi.mIncDirection[kek][1], mPainting.getHauteur());

      if (testCouleur((mApplis.mBaseImage != null) ? new Color(mApplis.mBaseImage.getRGB(i, j)) : mPainting.getCouleur(i, j))) {
        dir[k + 1] = 1;
      }
    }

    // tirage d'un nombre aléatoire permettant de savoir si la fourmi va suivre
    // ou non la couleur
    tirage = GenerateurAleatoire.nextFloat();

    // la fourmi suit la couleur
    if (((tirage <= mProba[3]) && ((dir[0] + dir[1] + dir[2]) > 0)) || ((dir[0] + dir[1] + dir[2]) == 3)) {
      prob1 = (dir[0]) * mProba[0];
      prob2 = (dir[1]) * mProba[1];
      prob3 = (dir[2]) * mProba[2];
    }
    // la fourmi ne suit pas la couleur
    else {
      prob1 = (1 - dir[0]) * mProba[0];
      prob2 = (1 - dir[1]) * mProba[1];
      prob3 = (1 - dir[2]) * mProba[2];
    }
    total = prob1 + prob2 + prob3;
    prob1 = prob1 / total;
    prob2 = prob2 / total + prob1;
    prob3 = prob3 / total + prob2;

    // incrémentation de la direction de la fourmi selon la direction choisie
    tirage = GenerateurAleatoire.nextFloat();// Math.random();

    mDirection = (tirage < prob1) ? (byte) modulo(mDirection - mDecalDir, 8)
        : ((tirage >= prob2) ? (byte) modulo(mDirection + mDecalDir, 8) : mDirection);

    x += CFourmi.mIncDirection[mDirection][0];
    y += CFourmi.mIncDirection[mDirection][1];

    x = modulo(x, mPainting.getLargeur());
    y = modulo(y, mPainting.getHauteur());

    // coloration de la nouvelle position de la fourmi
    mPainting.setCouleur(x, y, mCouleurDeposee, mTaille);

    mApplis.IncrementFpsCounter();
  }

  public long getNbDeplacements() {
    return mNbDeplacements;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  /*************************************************************************************************
   * Titre : modulo 
   * Description : Fonction de modulo permettant (entre autre) aux fourmis de
   * réapparaitre de l'autre côté du Canvas lorsqu'elles sortent de ce dernier.
   *
   * @param val valeur
   * @param module module
   *
   * @return int
   */
  private int modulo(int val, int module) {
    return (val + module) % module;
  }

  /*************************************************************************************************
   * Titre : boolean testCouleur() 
   * Description : fonction testant si la couleur passée en paramètre est 
   * dans le seuil de luminance de la couleur suivie par la fourmi
   * 
   * @param pCouleur couleur
   * 
   * @return boolean
   *
   */
  private boolean testCouleur(Color pCouleur) {
    return Math.abs(mLuminanceCouleurSuivie - (0.2126f * pCouleur.getRed() + 0.7152f * pCouleur.getGreen()
        + 0.0722f * pCouleur.getBlue())) < mSeuilLuminance;
  }
}
