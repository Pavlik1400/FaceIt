"""
Module for biometrical face authentification
"""


import face_recognition
import cv2
import numpy as np
import os
import json
from data_structs import UsersData


class Database:
    """
    Class for users database
    """
    def __init__(self):
        if 'auth_data.json' not in os.listdir('./'):
            with open('auth_data.json', 'w',  encoding='utf-8') as file:
                json.dump({}, file, ensure_ascii=False, indent=4)

        self.users = []
        self.known_face_encodings = []

        self.initialise_known_faces()

    def initialise_known_faces(self):
        """
        This method reads database
        """
        with open('auth_data.json', encoding='utf-8') as file:
            data = json.loads(file.read())

        for user_name in data:
            self.users.append(UsersData(user_name, np.asarray(data[user_name]['photo_encoded']), data[user_name]['right_level'], data[user_name]['passwd']))

        for user in self.users:
            self.known_face_encodings.append(user.get_photo())

    def new_user(self, webcam):
        """
        This method initialise new user in database
        """
        print("\n\nWelcome to New user Creator!")

        if self.users == []:
            print("Creating user with root (level 2)...")
            right_level = 2
        else:
            print("Creating user with usual rights (level 1)...")
            right_level = 1

        name = input("Please, enter your name: ")

        passwd = input("Please, enter your restore password: ")

        new_user = User(webcam)
        new_user.initialise_user_face()
        photo_encoded = new_user.face_encoding

        self.users.append(UsersData(name, photo_encoded, right_level, passwd))

        user_data = {}
        for user in self.users:
            user_data[user.return_name()] = {
                'photo_encoded': user.get_photo().tolist(),
                'right_level': user.return_right_level(),
                'passwd': user.return_passwd()
            }

        with open('auth_data.json', 'w', encoding='utf-8') as file:
            json.dump(user_data, file, ensure_ascii=False, indent=4)

        self.initialise_known_faces()

        print("Successfully created new user! Please, log in now!\n")


class User:
    """
    Class for representing user of device
    """
    def __init__(self, webcam):
        self.webcam = webcam
        self.video_capture = None
        self.face_encoding = None
        self.__is_logged = False
        self._name = None
        self._right_level = 0
        self.db = Database()

    def initialise_user_face(self):
        """
        This method reads biometrical info from user`s face
        """
        print('---------------------------------------------')
        input("Press ENTER to start capturing face...")

        face_locations = None
        self.video_capture = cv2.VideoCapture("/dev/video{}".format(str(self.webcam)))

        while not face_locations:
            ret, frame = self.video_capture.read()
            small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)
            rgb_small_frame = small_frame[:, :, ::-1]

            face_locations = face_recognition.face_locations(rgb_small_frame)
            try:
                self.face_encoding = face_recognition.face_encodings(rgb_small_frame, face_locations)[0]
            except IndexError:
                continue

        self.video_capture.release()

    def recognise_user(self):
        """
        This method looks for user`s face info in our database
        """
        matches = face_recognition.compare_faces(self.db.known_face_encodings, self.face_encoding)

        face_distances = face_recognition.face_distance(self.db.known_face_encodings, self.face_encoding)
        best_match_index = np.argmin(face_distances)

        if matches[best_match_index]:
            self._name = self.db.users[best_match_index].return_name()
            self._right_level = self.db.users[best_match_index].return_right_level()

    def login(self):
        """
        Method for logging in
        """
        if not self.db.users:
            print("Database is empty, please, create first user\n")
        else:
            while not self.__is_logged:
                self.initialise_user_face()
                self.recognise_user()
                if self._name:
                    self.__is_logged = True
                    print("Successfully logged in!\n")

    def some_func_that_works_if_logged(self):
        """
        This method is based on logging and here
        you should pate your code that should
        be done after logging in.
        Each user has it`s own level of rights
        """
        if self.__is_logged:
            if self._right_level == 2:
                print("logged as user with 2 level, {}".format(self._name))
                print("Adding another user...")
                self.db.new_user(self.webcam)
            else:
                print("logged as user with 1 level, {}".format(self._name))
        elif not self.db.users:
            print("not logged, please, create the first user")
            self.db.new_user(self.webcam)
            self.some_func_that_works_if_logged()
        else:
            print("not logged in")
            self.login()
            self.some_func_that_works_if_logged()

    def exit(self):
        """
        Method for exiting
        """
        self._name = None
        self._right_level = 0
        self.__is_logged = False
        print("Successfully exited!\n")
