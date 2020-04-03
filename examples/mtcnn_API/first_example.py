"""
This is example module that shows mtcnn functionality
It has Identifier class that can find faces with mtcnn library,
save results to json show them with matplotlib.pyplot,
reveal face from your web cam

Sources: 1. Official documentation: https://pypi.org/project/mtcnn/
         2. How to Perform Face Detection with Deep Learning by Jason Brownlee:
            https://machinelearningmastery.com/how-to-perform-face-detection-with-classical-and-deep-learning-methods-in-python-with-keras/
         3. A guide to Face Detection in Python by Mael Fabien:
            https://towardsdatascience.com/a-guide-to-face-detection-in-python-3eab0f6b9fc1
"""


from matplotlib import pyplot
from matplotlib.patches import Rectangle
from matplotlib.patches import Circle
from mtcnn.mtcnn import MTCNN
import json
import cv2


class Identifier:
    """
    Class for finding faces on photo and stream video
    """
    def __init__(self):
        """
        __filename - name of image, where you want to find faces
        detector - just instance of MTCNN class, needed for finding faces
        """
        self.__filename = "test_data/group-1.jpg"
        self._detector = MTCNN(scale_factor=0.95)

    @property
    def filename(self):
        return self.__filename

    @filename.setter
    def filename(self, new_name):
        if not isinstance(new_name, str):
            print("Wrong name!")
            return -1
        try:
            f = open(new_name, 'r')
            f.close()
        except FileNotFoundError:
            print(f"There is no {new_name} picture!")

    def find_faces(self):
        """
        :return: list with found faces, eyes, mouth, nose
        """
        # read image with pyplot
        data = pyplot.imread(self.filename)
        # find faces using MTCNN.detect_faces()
        faces = self._detector.detect_faces(data)
        return faces

    def show_faces(self, face_data=None):
        """
        Shows faces on the image using matplotlib.pyplot
        :param face_data: data about faces. It is created by self.find_faces()
        :return: None
        """
        if face_data is None:
            face_data = self.find_faces()

        # read image
        img_data = pyplot.imread(self.filename)
        # show image
        pyplot.imshow(img_data)

        ax = pyplot.gca()
        # draw each face
        for face in face_data:
            # get coordinates
            x_coord, y_coord, width, height = face['box']
            # create the box around face
            rect = Rectangle((x_coord, y_coord), width, height, fill=False, color='blue')
            # draw the box
            ax.add_patch(rect)
            # draw the dots of eyes, nose, and mouth
            for key, value in face['keypoints'].items():
                # create and draw dot
                dot = Circle(value, radius=1, color='blue')
                ax.add_patch(dot)
        # show the recognised faces
        pyplot.show()

    def save_faces(self, face_results=None):
        if face_results is None:
            face_results = self.find_faces()
        with open("results.json", 'w') as res_file:
            json.dump(face_results, res_file, indent=3)

    def web_camera_stream(self, web_camera_path, verbose=False):
        font = cv2.FONT_HERSHEY_SIMPLEX  # font for text writting
        # capture video from web cam
        video_capture = cv2.VideoCapture(web_camera_path)

        while True:
            # capture frame by frame
            ret, frame = video_capture.read()

            # convert to rgb:
            frame_data = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            # look for faces
            face_data = self._detector.detect_faces(frame_data)
            if verbose:
                print(face_data)

            # Draw a rectangle around the faces
            for face in face_data:
                x_coord, y_coord, width, height = face["box"]
                # draw rectangle around face
                cv2.rectangle(frame, (x_coord, y_coord), (x_coord + width, y_coord + height), (255, 0, 0), 3)
                roi_color = frame[y_coord:y_coord + height, x_coord:x_coord + width]
                # type face near found face
                cv2.putText(frame, "Face", (x_coord, y_coord), font, 2, (255, 0, 0), 3)

                for key, value in face['keypoints'].items():
                    # for each feature (nose, eye, mouth) draw rectangle and text
                    cv2.rectangle(roi_color, (0, 0), (value[0], value[1]), (255, 0, 0), 2)
                    cv2.putText(frame, key, (value[0], value[1]), 1, 1, (0, 255, 0), 1)

            cv2.putText(frame, 'Number of Faces : ' + str(len(face_data)), (40, 40), font, 1, (255, 0, 0), 2)
            # Display the resulting frame
            cv2.imshow('Video', frame)

            # if 'q': exit
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break


if __name__ == '__main__':
    identifier1 = Identifier()
    # filename = "test_data/" + input("Type picture name (from test_data, with .jpg): ")
    # identifier1.filename = filename
    # face_results = identifier1.find_faces()
    # identifier1.show_faces()
    identifier1.web_camera_stream(0, verbose=True)  # Important!!! in your device number can be another
