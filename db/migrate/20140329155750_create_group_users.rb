Sequel.migration do
  up do
    create_table(:group_users) do
      primary_key [:group_id, :user_id]

      # this foreign key constraint does not get created
      foreign_key :group_id, :groups, :foreign_key_constraint_name => 'group_users_fkey_group_id'
      foreign_key :user_id, :users, :foreign_key_constraint_name => 'group_users_fkey_user_id'

      DateTime :updated_at
      DateTime :created_at

    end
  end

  down do
    drop_table(:group_users)
  end
end
