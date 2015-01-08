original = [["A", "B"], ["C", "D"], ["E", "F"]] 

new = [["A", "B"],["D","F"],["C", "E"]]

P = [[1.0,0.0,0.0],
	 [0.0,1.0,0.0],
	 [0.0,0.0,1.0]]

Q = [[0.0,0.0,0.0],[0.0,0.0,0.0],[0.0,0.0,0.0]]

def intersection_len(x, y):
	c = 0.0;
	for a in x:
		if a in y:
			c += 1.0
	return c
def test():
  for i in range(3):
    for j in range(3):
      for x in range(3):
        for y in range(3):
    	  Q[i][j] += (intersection_len(original[x], new[i])/len(new[i])) * (intersection_len(original[y], new[j])/len(new[j])) * P[x][y]


# Result
# [1.0, 0.0, 0.0]
# [0.0, 0.5, 0.5]
# [0.0, 0.5, 0.5]

if __name__ == '__main__':
	test()
	print Q