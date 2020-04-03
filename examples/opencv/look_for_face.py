import os
import cv2


# Use webcam video
cap = cv2.VideoCapture(0)
cap.set(3, 1920)
cap.set(4, 1080)
face_cascade = cv2.CascadeClassifier(os.path.join(os.getcwd(), 'haarcascade_frontalface_default.xml'))

while True:
    # Capture frame-by-frame
    ret, frame = cap.read()
    # Our operations on the frame come here
    frame_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    frame_gray = cv2.equalizeHist(frame_gray)
    # Detect faces
    faces = face_cascade.detectMultiScale(frame_gray)
    for (x, y, w, h) in faces:
        frame = cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)
        roi_gray = frame[y:y + h, x:x + w]
        roi_color = frame[y:y + h, x:x + w]

    # Display the resulting frame
    # Press any button to stop
    cv2.imshow('MyWebCam', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything done, release the capture
cap.release()
cv2.destroyAllWindows()

