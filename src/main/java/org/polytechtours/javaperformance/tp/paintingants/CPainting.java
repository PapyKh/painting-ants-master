package org.polytechtours.javaperformance.tp.paintingants;
// package PaintingAnts_v2;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;

// version : 2.0

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * <p>
 * Titre : Painting Ants
 * </p>
 * <p>
 * Description :
 * </p>
 * <p>
 * Copyright : Copyright (c) 2003
 * </p>
 * <p>
 * Société : Equipe Réseaux/TIC - Laboratoire d'Informatique de l'Université de
 * Tours
 * </p>
 *
 * @author Nicolas Monmarché
 * @version 1.0
 */

public class CPainting extends Canvas implements MouseListener {
  private static final long serialVersionUID = 1L;
  // matrice servant pour le produit de convolution
  final static private float[][] mMatriceConv9 = {{1 / 16f, 2 / 16f, 1 / 16f}, {2 / 16f, 4 / 16f, 2 / 16f}, {1 / 16f, 2 / 16f, 1 / 16f}};
  final static private float[][] mMatriceConv25 = {{1 / 44f, 1 / 44f, 2 / 44f, 1 / 44f, 1 / 44f}, {1 / 44f, 2 / 44f, 3 / 44f, 2 / 44f, 1 / 44f},
  {2 / 44f, 3 / 44f, 4 / 44f, 3 / 44f, 2 / 44f}, {1 / 44f, 2 / 44f, 3 / 44f, 2 / 44f, 1 / 44f},
  {1 / 44f, 1 / 44f, 2 / 44f, 1 / 44f, 1 / 44f}};
  final static private float[][] mMatriceConv49 = {{1 / 128f, 1 / 128f, 2 / 128f, 2 / 128f, 2 / 128f, 1 / 128f, 1 / 128f},
  {1 / 128f, 2 / 128f, 3 / 128f, 4 / 128f, 3 / 128f, 2 / 128f, 1 / 128f},
  {2 / 128f, 3 / 128f, 4 / 128f, 5 / 128f, 4 / 128f, 3 / 128f, 2 / 128f},
  {2 / 128f, 4 / 128f, 5 / 128f, 8 / 128f, 5 / 128f, 4 / 128f, 2 / 128f},
  {2 / 128f, 3 / 128f, 4 / 128f, 5 / 128f, 4 / 128f, 3 / 128f, 2 / 128f},
  {1 / 128f, 2 / 128f, 3 / 128f, 4 / 128f, 3 / 128f, 2 / 128f, 1 / 128f},
  {1 / 128f, 1 / 128f, 2 / 128f, 2 / 128f, 2 / 128f, 1 / 128f, 1 / 128f}};
  // Objet de type Graphics permettant de manipuler l'affichage du Canvas
  private Graphics mGraphics;
  // Objet ne servant que pour les bloc synchronized pour la manipulation du
  // tableau des couleurs
  final private Object mMutexCouleurs = new Object();
  // tableau des couleurs, il permert de conserver en memoire l'état de chaque
  // pixel du canvas, ce qui est necessaire au deplacemet des fourmi
  // il sert aussi pour la fonction paint du Canvas
  final private Color[][] mCouleurs;
  // couleur du fond
  final private Color mCouleurFond = new Color(255, 255, 255);
  // dimensions
  final private Dimension mDimension;

  final private PaintingAnts mApplis;

  private boolean mSuspendu = false;

  /******************************************************************************
   * Titre : public CPainting() Description : Constructeur de la classe
   ******************************************************************************/
  public CPainting(Dimension pDimension, PaintingAnts pApplis) {
    int i, j;
    addMouseListener(this);
    mApplis = pApplis;
    mDimension = pDimension;

    setBounds(new Rectangle(0, 0, mDimension.width, mDimension.height));

    this.setBackground(mCouleurFond);

    // initialisation de la matrice des couleurs
    mCouleurs = new Color[mDimension.width][mDimension.height];
    synchronized (mMutexCouleurs) {
      for (i = 0; i != mDimension.width; i++) {
        for (j = 0; j != mDimension.height; j++) {
          mCouleurs[i][j] = mCouleurFond;
        }
      }
    }
  }

  /******************************************************************************
   * Titre : Color getCouleur Description : Cette fonction renvoie la couleur
   * d'une case
   ******************************************************************************/
  public Color getCouleur(int x, int y) {
    synchronized (mMutexCouleurs) {
      return mCouleurs[x][y];
    }
  }

  /******************************************************************************
   * Titre : Color getHauteur Description : Cette fonction renvoie la hauteur de
   * la peinture
   ******************************************************************************/
  public int getHauteur() {
    return mDimension.height;
  }

  /******************************************************************************
   * Titre : Color getLargeur Description : Cette fonction renvoie la hauteur de
   * la peinture
   ******************************************************************************/
  public int getLargeur() {
    return mDimension.width;
  }

  /******************************************************************************
   * Titre : void init() Description : Initialise le fond a la couleur blanche
   * et initialise le tableau des couleurs avec la couleur blanche
   ******************************************************************************/
  public void init() {
    int i, j;
    mGraphics = getGraphics();
    synchronized (mMutexCouleurs) {
      mGraphics.clearRect(0, 0, mDimension.width, mDimension.height);

      // initialisation de la matrice des couleurs
      for (i = 0; i != mDimension.width; i++) {
        for (j = 0; j != mDimension.height; j++) {
          mCouleurs[i][j] = mCouleurFond;
        }
      }
    }

    mSuspendu = false;
  }

  /****************************************************************************/
  public void mouseClicked(MouseEvent pMouseEvent) {
    pMouseEvent.consume();
    if (pMouseEvent.getButton() == MouseEvent.BUTTON1) {
      // double clic sur le bouton gauche = effacer et recommencer
      if (pMouseEvent.getClickCount() == 2) {
        init();
      }
      // simple clic = suspendre les calculs et l'affichage
      mApplis.pause();
    } else {
      // bouton du milieu (roulette) = suspendre l'affichage mais
      // continuer les calculs
      if (pMouseEvent.getButton() == MouseEvent.BUTTON2) {
        suspendre();
      } else {
        // clic bouton droit = effacer et recommencer
        // case pMouseEvent.BUTTON3:
        init();
      }
    }
  }

  /****************************************************************************/
  public void mouseEntered(MouseEvent pMouseEvent) {
  }

  /****************************************************************************/
  public void mouseExited(MouseEvent pMouseEvent) {
  }

  /****************************************************************************/
  public void mousePressed(MouseEvent pMouseEvent) {
  }

  /****************************************************************************/
  public void mouseReleased(MouseEvent pMouseEvent) {
  }

  /******************************************************************************
   * Titre : void paint(Graphics g) Description : Surcharge de la fonction qui
   * est appelé lorsque le composant doit être redessiné
   ******************************************************************************/
  @Override
  public void paint(Graphics pGraphics) {
    int i, j;

    synchronized (mMutexCouleurs) {
      for (i = 0; i < mDimension.width; i++) {
        for (j = 0; j < mDimension.height; j++) {
          pGraphics.setColor(mCouleurs[i][j]);
          pGraphics.fillRect(i, j, 1, 1);
        }
      }
    }
  }

  public void paintColor(int x, int y, int pSize, float[][] pMatrice) {
    int i, j, k, l, m, n;
    float R, G, B;
    Color lColor;
    // produit de convolution discrete sur 9 cases
    for (i = 0; i < pSize; i++) {
      for (j = 0; j < pSize; j++) {
        R = G = B = 0f;

        for (k = 0; k < pSize; k++) {
          for (l = 0; l < pSize; l++) {
            m = (x + i + k - (pSize - 1) + mDimension.width) % mDimension.width;
            n = (y + j + l - (pSize - 1) + mDimension.height) % mDimension.height;
            R += pMatrice[k][l] * mCouleurs[m][n].getRed();
            G += pMatrice[k][l] * mCouleurs[m][n].getGreen();
            B += pMatrice[k][l] * mCouleurs[m][n].getBlue();
          }
        }
        lColor = new Color((int) R, (int) G, (int) B);

        mGraphics.setColor(lColor);

        m = (x + i - pSize / 2 + mDimension.width) % mDimension.width;
        n = (y + j - pSize / 2 + mDimension.height) % mDimension.height;
        mCouleurs[m][n] = lColor;
        if (!mSuspendu) {
          mGraphics.fillRect(m, n, 1, 1);
        }
      }
    }
  }

  /******************************************************************************
   * Titre : void colorer_case(int x, int y, Color c) Description : Cette
   * fonction va colorer le pixel correspondant et mettre a jour le tabmleau des
   * couleurs
   ******************************************************************************/
  public void setCouleur(int x, int y, Color c, int pTaille) {
    synchronized (mMutexCouleurs) {
      if (!mSuspendu) {
        // on colorie la case sur laquelle se trouve la fourmi
        mGraphics.setColor(c);
        mGraphics.fillRect(x, y, 1, 1);
      }

      mCouleurs[x][y] = c;

      // on fait diffuser la couleur :
      switch (pTaille) {
        case 1:
            paintColor(x, y, pTaille, mMatriceConv9);
          break;
        case 2:
            paintColor(x, y, pTaille, mMatriceConv25);
          break;
        case 3:
            paintColor(x, y, pTaille, mMatriceConv49);
          break;
        default:
          break;
      }
    }
  }

  /******************************************************************************
   * Titre : setSupendu Description : Cette fonction change l'état de suspension
   ******************************************************************************/

  public void suspendre() {
    mSuspendu = !mSuspendu;
    if (!mSuspendu) {
      repaint();
    }
  }
}
