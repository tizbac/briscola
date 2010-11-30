# -*- coding: utf-8 -*-
import sys
import string
import pygame
from pygame.locals import *
from core import *
def load_sound(name):
    class NoneSound:
        def play(self): pass
    if not pygame.mixer:
        return NoneSound()
    fullname = os.path.join('data', name)
    try:
        sound = pygame.mixer.Sound(fullname)
    except pygame.error, message:
        print "Cannot load sound:"
        raise SystemExit, message
    return sound
if len(sys.argv) > 1:
	fs = sys.argv[1]
else:
	fs = 1
class regioneschermo:
  def __init__(self,top,bottom,left,right):
    self.top = top
    self.bottom = bottom
    self.left = left
    self.right = right
  def over(self,x,y):
    return (y >= self.top and y <= self.bottom and x >= self.left and x <= self.right)
pygame.init()
window = pygame.display.set_mode((800, 600),pygame.FULLSCREEN*int(fs)) 
screen = pygame.display.get_surface() 
background = pygame.Surface(screen.get_size())
background = background.convert()
background.fill((0, 0,0))
carta = pygame.image.load(os.path.join("immagini","carta.jpg"))
reg = []

mouseover = dict()
par = Partita(gioc)
par.blinktks = 0
par.blinkb = False
	   
halfw = (58*3)/2
ss = 400-halfw
i = 0
font1 = pygame.font.Font(None, 36)
font2 = pygame.font.Font(None, 34)
for c in par.gic[0]:
  reg.append(regioneschermo(600-99,600,ss+i*60,ss+i*60+58))
  mouseover.update([(i,False)])
  i += 1
regnantei = pygame.transform.rotate(par.regnante.immagine,90)
def render():
  inp(pygame.event.get())
  time.sleep(0.0167)
  halfw = (58*3)/2
  ss = 400-halfw
  if pygame.font:
    font = pygame.font.Font(None, 36)
    textpl = font1.render("%i Punti" % calcolapunti(par.prese[0]), 1, (255, 255, 10))
    textcomp = font1.render("%i Punti" % calcolapunti(par.prese[1]), 1, (255, 255, 10))
  screen.blit(background, (0, 0))
  if regnantei not in par.gic[0] and regnantei not in par.gic[1]:
    screen.blit(regnantei, (99/2,600/2-58/2))
  if len(par.mazzo) > 0:
    screen.blit(carta, (99/2-58/2,600/2-99/2))
  screen.blit(textpl, (700,500))
  screen.blit(textcomp, (700,100))
  i = 0
  for c in par.gic[0]:
    if i < len(par.gic[1]):
      screen.blit(carta, (ss+i*80,0))
    screen.blit(c.immagine, (ss+i*60,600-99-10*int(mouseover[i])))
    i += 1
  i = 0
  for c_ in par.sultavolo:
    c = par.sultavolo[c_]
    screen.blit(c.immagine, (ss+i*60,600-299))
    i += 1
  if (len(par.gic[0]) == 0 and len(par.gic[1]) == 0):
  	par.blinktks += 1
  	if par.blinktks > 20:
  		par.blinktks = 0
  		par.blinkb = not par.blinkb
  	endtxt = font2.render("La partita e' finita, Premere SPAZIO Per Farne un'altra o ESC per uscire" , 1, (255, 0, 10))
  	if par.blinkb: screen.blit(endtxt, (0,200))
  pygame.display.flip()
def inp(events): 
   for event in events: 
      if event.type == QUIT: 
         sys.exit(0) 
      elif event.type == 4:
	i = 0
        for r in reg:
	  mouseover[i] = r.over(event.pos[0],event.pos[1])
	  i+= 1
      elif event.type == 6:
	i = 0
        for r in reg:
	  mouseover[i] = r.over(event.pos[0],event.pos[1])
	  i+= 1
	i = 0
	for m in mouseover:
	  if mouseover[m]:
	    par.butta(0,m,render)
      elif event.type==3 and event.key == 27:
	raise SystemExit(0)
      elif event.type==3 and event.key == 32:
      	global par
      	global regnantei
	par = Partita(gioc)
	par.blinktks = 0
	par.blinkb = False
	regnantei = pygame.transform.rotate(par.regnante.immagine,90)
      else:
	print event
while 1:
  #print "A tavola c'è :"+ par.regnante.nomeC()+" %i Carte rimanenti nel mazzo + 1 a tavola" % len(par.mazzo)
  #print "Hai "+' || '.join(listacarte(par.gic[0]))
  if par.mappaturni[par.turno] == 0 or (len(par.gic[0]) == 0 and len(par.gic[1]) == 0):
    render()
  elif par.mappaturni[par.turno] == 1:
    print "Sta al computer"
    puntit = 0
    butta = -1
    briscolebuttate = [0]
    for c in par.sultavolo:
      puntit += par.sultavolo[c].punti
    for c in par.sultavolo:
      if par.sultavolo[c].seme == par.regnante.seme:
	briscolebuttate.append(par.sultavolo[c].forza)

    if briscola(par.regnante,par.gic[1]):
      if puntit > 1 and puntit < 10 and max(briscolebuttate) < 6:
	r = pic(par.gic[1],par.regnante.seme,max(briscolebuttate))
	if r and par.gic[1][r].forza < 6:
	  butta = r
      if puntit > 9:
	i = 0
	for c in par.gic[1]:
	  if c.seme == par.regnante.seme and c.forza > max(briscolebuttate):
	    butta = i
	    break
	  i += 1
    briscole = []
    carichi = []
    puntarelli = []
    lisci = []
    i = 0
    for c in par.gic[1]:
      if c.seme == par.regnante.seme:
	briscole.append(i)
      if c.punti > 4:
	carichi.append(i)
      if c.punti == 0 and not c in puntarelli and not c in briscole and not c in carichi:
	lisci.append(i)
      if c.punti > 0 and c.punti < 10 and not c in briscole:
	puntarelli.append(i)
      i += 1
    if len(par.sultavolo) >  0:
      for ca in carichi:
	if par.gic[1][ca].seme != par.regnante.seme and par.sultavolo[par.mappaturni[0]].seme == par.gic[1][ca].seme and par.sultavolo[par.mappaturni[0]].forza < par.gic[1][ca].forza:
	  butta = ca
    if butta == -1 and len(lisci) > 0:
      butta = lisci[0]
    elif len(puntarelli) > 0:
      butta = puntarelli[0]
    elif len(carichi) > 0:
      for c in range(0,len(par.gic[1])):
	if par.gic[1][c].punti == 10:
	  butta = c
      if butta == -1:
	for c in range(0,len(par.gic[1])):
	  if par.gic[1][c].seme == par.regnante.seme:
	    butta = c
    elif len(briscole) > 0:
      cl = 100
      for c in range(0,len(par.gic[1])):
	  if par.gic[1][c].forza < cl:
	    butta = c
	
    if butta == -1:
      briscole = []
      carichi = []
      puntarelli = []
      lisci = []
      i = 0
      for c in par.gic[1]:
	if c.seme == par.regnante.seme:
	  briscole.append(i)
	if c.punti > 4:
	  carichi.append(i)
	if c.punti == 0 and not c in puntarelli and not c in briscole and not c in carichi:
	  lisci.append(i)
	if c.punti > 0 and c.punti < 10 and not c in briscole:
	  puntarelli.append(i)
	i += 1
      if len(lisci) > 0:
	butta = lisci[0]
      elif len(puntarelli) > 0:
	butta = puntarelli[0]
      elif len(carichi) > 0:
	for c in range(0,len(par.gic[1])):
	  if par.gic[1][c].punti == 10:
	    butta = c
	if butta == -1:
	  for c in range(0,len(par.gic[1])):
	    if par.gic[1][c].seme == par.regnante.seme:
	      butta = c
      elif len(briscole) > 0:
	cl = 100
	for c in range(0,len(par.gic[1])):
	    if par.gic[1][c].forza < cl:
	      butta = c
    par.butta(1,butta,render)
    
